package uk.gov.hmrc.eos.eutu55.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmrc.eos.eutu55.entity.SubscriptionStatus;
import uk.gov.hmrc.eos.eutu55.helper.XmlTestMarshaller;
import uk.gov.hmrc.eos.eutu55.model.SubscriptionRequest;
import uk.gov.hmrc.eos.eutu55.model.SubscriptionResponse;
import uk.gov.hmrc.eos.eutu55.model.SubscriptionStatusResponse;
import uk.gov.hmrc.eos.eutu55.repository.SubscriptionStatusRepository;
import uk.gov.hmrc.eos.eutu55.utils.SubscriptionStatusType;
import uk.gov.hmrc.eu.eutu55.subscription.UpdateSubscriptionReqMsg;
import uk.gov.hmrc.eu.eutu55.subscription.UpdateSubscriptionRespMsg;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmrc.eos.eutu55.helper.TestHelper.response;
import static uk.gov.hmrc.eos.eutu55.utils.AdminActionType.SUBSCRIPTION;
import static uk.gov.hmrc.eos.eutu55.utils.Outcome.ACCEPTED;
import static uk.gov.hmrc.eos.eutu55.utils.Outcome.REJECTED;
import static uk.gov.hmrc.eos.eutu55.utils.SubscriptionStatusType.OFF;

@ExtendWith(MockitoExtension.class)
class SubscriptionStatusServiceTest {

    private static final String BEARER_TOKEN = "f916fa9b-0500-3837-a321-4c76f67499e0";
    private final LocalDateTime REQUEST_TIME = LocalDateTime.of(2022, Month.MAY, 10, 10, 10, 10);
    private String endpoint = "eis/endpoint";
    private static final String USER_ID = "TEST_USER";

    @Mock
    private SubscriptionStatusRepository subscriptionStatusRepository;
    @Mock
    private GatewayService gatewayService;
    @Mock
    private AdminAuditService adminAuditService;
    @Captor
    ArgumentCaptor<UpdateSubscriptionReqMsg> eisRequestPayloadCaptor;

    @InjectMocks
    private SubscriptionStatusService service;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(service, "endpoint", endpoint);
        ReflectionTestUtils.setField(service, "bearerToken", BEARER_TOKEN);
    }

    @Test
    @DisplayName("Update the subscription status for an existing status record")
    void testSaveOrUpdateForExistingSubscriptionStatusRecord() {
        //Given
        when(subscriptionStatusRepository.findById(1)).thenReturn(subscriptionStatusRecord());

        //When
        SubscriptionStatus actualSubscriptionStatus = service.saveOrUpdateStatus(OFF);

        //Then
        assertThat(actualSubscriptionStatus.getId(), equalTo(1));
        assertThat(actualSubscriptionStatus.getStatus(), equalTo(OFF));
        verify(subscriptionStatusRepository).findById(1);
        verify(subscriptionStatusRepository, times(1)).save(any(SubscriptionStatus.class));
    }

    @Test
    @DisplayName("Create a subscription status record if not available already")
    void testSaveOrUpdateForNewSubscriptionStatusRecord() {
        //Given
        when(subscriptionStatusRepository.findById(1)).thenReturn(Optional.empty());

        //When
        SubscriptionStatus subscriptionStatus = service.saveOrUpdateStatus(SubscriptionStatusType.ON);

        //Then
        assertThat(subscriptionStatus, notNullValue());
        assertThat(subscriptionStatus.getId(), equalTo(1));
        assertThat(subscriptionStatus.getStatus(), equalTo(SubscriptionStatusType.ON));
        verify(subscriptionStatusRepository).findById(1);
        verify(subscriptionStatusRepository, times(1)).save(any(SubscriptionStatus.class));
    }

    @Test
    @DisplayName("Subscription status record can be retrieved if exists")
    void subscriptionStatusRecordCanBeRetrievedIfExists() {
        when(subscriptionStatusRepository.findById(1)).thenReturn(subscriptionStatusRecord());

        SubscriptionStatusResponse subscriptionStatus = service.retrieveSubscriptionStatus();

        assertThat(subscriptionStatus.getStatus(), equalTo(SubscriptionStatusType.ON));
        verify(subscriptionStatusRepository).findById(1);
        verifyNoMoreInteractions(subscriptionStatusRepository);
    }

    @Test
    @DisplayName("Empty record should be returned when subscription status record does not exist")
    void noResultsFoundExceptionThrownWhenSubscriptionStatusRecordNotExist() {
        when(subscriptionStatusRepository.findById(1)).thenReturn(Optional.empty());
        SubscriptionStatusResponse subscriptionStatus = service.retrieveSubscriptionStatus();
        assertThat(subscriptionStatus, is(notNullValue()));
        assertThat(subscriptionStatus.getStatus(), is(nullValue()));
        assertThat(subscriptionStatus.getDate(), is(nullValue()));
    }

    @Test
    @DisplayName("Subscription request can be transformed and passed to EIS and status updated in PDS when the outcome is accepted")
    void subscriptionRequestTransformedAndStatusUpdatedInPDSWhenOutcomeIsAccepted() {
        when(subscriptionStatusRepository.findById(1)).thenReturn(subscriptionStatusRecord());
        UpdateSubscriptionRespMsg subscriptionResponseMsg = XmlTestMarshaller.unmarshall(response("subscription/accept.xml"), UpdateSubscriptionRespMsg.class);
        when(gatewayService.post(eq(endpoint), eisRequestPayloadCaptor.capture(), eq(UpdateSubscriptionRespMsg.class), eq(BEARER_TOKEN))).thenReturn(subscriptionResponseMsg);

        SubscriptionRequest request = new SubscriptionRequest(OFF, "test@email.com");
        SubscriptionResponse response = service.saveOrUpdateStatus(request, USER_ID);

        UpdateSubscriptionReqMsg eisRequestPayload = eisRequestPayloadCaptor.getValue();
        assertAll("EIS Request Payload",
                () -> assertThat(eisRequestPayload.getContactEMail(), equalTo(request.getContactEmail())),
                () -> assertThat(eisRequestPayload.getNewStatus(), equalTo(request.getStatus().getValue())));

        verify(subscriptionStatusRepository, times(1)).save(any(SubscriptionStatus.class));
        verify(adminAuditService, times(1)).save(SUBSCRIPTION, ACCEPTED, USER_ID);

        assertAll("Subscription Response",
                () -> assertThat(response.getOutcome(), equalTo(ACCEPTED)));
    }

    @Test
    @DisplayName("Subscription request can be transformed and passed to EIS and status is not updated when the outcome is rejected")
    void subscriptionRequestTransformedAndStatusNotUpdatedInPDSWhenOutcomeIsRejected() {
        UpdateSubscriptionRespMsg subscriptionResponseMsg = XmlTestMarshaller.unmarshall(response("subscription/reject.xml"), UpdateSubscriptionRespMsg.class);
        when(gatewayService.post(eq(endpoint), eisRequestPayloadCaptor.capture(), eq(UpdateSubscriptionRespMsg.class), eq(BEARER_TOKEN))).thenReturn(subscriptionResponseMsg);

        SubscriptionRequest request = new SubscriptionRequest(OFF, "test@email.com");
        SubscriptionResponse response = service.saveOrUpdateStatus(request, USER_ID);

        UpdateSubscriptionReqMsg eisRequestPayload = eisRequestPayloadCaptor.getValue();
        assertAll("EIS Request Payload",
                () -> assertThat(eisRequestPayload.getContactEMail(), equalTo(request.getContactEmail())),
                () -> assertThat(eisRequestPayload.getNewStatus(), equalTo(request.getStatus().getValue())));

        verify(subscriptionStatusRepository, never()).save(any(SubscriptionStatus.class));
        verify(adminAuditService, times(1)).save(SUBSCRIPTION, REJECTED, USER_ID);

        assertAll("Subscription Response",
                () -> assertThat(response.getOutcome(), equalTo(REJECTED)));
    }

    private Optional<SubscriptionStatus> subscriptionStatusRecord() {
        return Optional.of(SubscriptionStatus.builder()
                .id(1)
                .status(SubscriptionStatusType.ON)
                .requestTime(REQUEST_TIME)
                .build());
    }

}
