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
import uk.gov.hmrc.eos.eutu55.helper.XmlTestMarshaller;
import uk.gov.hmrc.eos.eutu55.model.RetrievalRequest;
import uk.gov.hmrc.eos.eutu55.model.RetrievalResultResponse;
import uk.gov.hmrc.eos.eutu55.utils.DateUtil;
import uk.gov.hmrc.eu.eutu55.retrieval.RetrievalResponse;

import java.time.LocalDate;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmrc.eos.eutu55.helper.TestHelper.response;
import static uk.gov.hmrc.eos.eutu55.utils.AdminActionType.RETRIEVAL;
import static uk.gov.hmrc.eos.eutu55.utils.Outcome.ACCEPTED;
import static uk.gov.hmrc.eos.eutu55.utils.Outcome.REJECTED;
import static uk.gov.hmrc.eos.eutu55.utils.SubscriptionStatusType.RECOVERY;

@ExtendWith(MockitoExtension.class)
class RetrievalServiceTest {

    @Mock
    private SubscriptionStatusService subscriptionStatusService;
    @Mock
    private AdminAuditService adminAuditService;
    @Mock
    private GatewayService gatewayService;

    @InjectMocks
    private RetrievalService service;

    @Captor
    ArgumentCaptor<uk.gov.hmrc.eu.eutu55.retrieval.RetrievalRequest> eisRequestPayloadCaptor;

    private String endpoint = "some/endpoint";
    private static final String BEARER_TOKEN = "f916fa9b-0500-3837-a321-4c76f67499e0";
    private static final String USER_ID = "TEST_USER";

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(service, "endpoint", endpoint);
        ReflectionTestUtils.setField(service, "bearerToken", BEARER_TOKEN);
    }

    @Test
    @DisplayName("Retrieval request can be posted to EIS and response can be persisted and Subscription status is updated when outcome is accepted")
    void retrievalRequestCanBePostedToEISAndResponseCanBePersistedAndSubscriptionStatusUpdatedOnAcceptedOutcome() {
        RetrievalResponse retrievalRespMsg = XmlTestMarshaller.unmarshall(response("retrieval/success.xml"), RetrievalResponse.class);
        when(gatewayService.post(eq(endpoint), eisRequestPayloadCaptor.capture(), eq(RetrievalResponse.class), eq(BEARER_TOKEN))).thenReturn(retrievalRespMsg);

        LocalDate requestFromDate = LocalDate.parse("2022-04-11");

        RetrievalResultResponse response = service.processRetrieval(new RetrievalRequest(requestFromDate), USER_ID);

        uk.gov.hmrc.eu.eutu55.retrieval.RetrievalRequest eisRequestPayload = eisRequestPayloadCaptor.getValue();
        assertAll("EIS Request Payload",
                () -> assertThat(DateUtil.localDateFromXmlDate(eisRequestPayload.getFromDate()), equalTo(requestFromDate)));

        verify(adminAuditService, times(1)).save(RETRIEVAL, ACCEPTED, USER_ID);
        verify(subscriptionStatusService, times(1)).saveOrUpdateStatus(RECOVERY);

        assertAll("Retrieval Response",
                () -> assertThat(response.getOutcome(), equalTo(ACCEPTED)));
    }

    @Test
    @DisplayName("Retrieval request can be posted to EIS and response can be persisted and Subscription status is not updated when outcome is rejected")
    void retrievalRequestCanBePostedToEISAndResponseCanBePersistedAndSubscriptionStatusNotUpdatedOnRejectedOutcome() {
        RetrievalResponse retrievalRespMsg = XmlTestMarshaller.unmarshall(response("retrieval/reject.xml"), RetrievalResponse.class);
        when(gatewayService.post(eq(endpoint), eisRequestPayloadCaptor.capture(), eq(RetrievalResponse.class), eq(BEARER_TOKEN))).thenReturn(retrievalRespMsg);

        LocalDate requestFromDate = LocalDate.parse("2022-04-11");
        RetrievalResultResponse response = service.processRetrieval(new RetrievalRequest(requestFromDate), USER_ID);

        uk.gov.hmrc.eu.eutu55.retrieval.RetrievalRequest eisRequestPayload = eisRequestPayloadCaptor.getValue();
        assertAll("EIS Request Payload",
                () -> assertThat(DateUtil.localDateFromXmlDate(eisRequestPayload.getFromDate()), equalTo(requestFromDate)));

        verify(adminAuditService, times(1)).save(RETRIEVAL, REJECTED, USER_ID);
        verify(subscriptionStatusService, never()).saveOrUpdateStatus(RECOVERY);

        assertAll("Retrieval Response",
                () -> assertThat(response.getOutcome(), equalTo(REJECTED)));
    }

}