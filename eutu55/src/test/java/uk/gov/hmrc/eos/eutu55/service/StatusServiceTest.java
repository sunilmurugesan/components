package uk.gov.hmrc.eos.eutu55.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmrc.eos.eutu55.helper.XmlTestMarshaller;
import uk.gov.hmrc.eos.eutu55.model.StatusResultResponse;
import uk.gov.hmrc.eu.eutu55.status.StatusResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmrc.eos.eutu55.helper.TestHelper.response;
import static uk.gov.hmrc.eos.eutu55.utils.AdminActionType.STATUS;
import static uk.gov.hmrc.eos.eutu55.utils.Outcome.ACCEPTED;
import static uk.gov.hmrc.eos.eutu55.utils.Outcome.REJECTED;

@ExtendWith(MockitoExtension.class)
class StatusServiceTest {

    private String endpoint = "eis/endpoint";
    private static final String BEARER_TOKEN = "f916fa9b-0500-3837-a321-4c76f67499e0";
    private static final String USER_ID = "TEST_USER";

    @Mock
    private AdminAuditService adminAuditService;
    @Mock
    private GatewayService gatewayService;


    @InjectMocks
    private StatusService service;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(service, "endpoint", endpoint);
        ReflectionTestUtils.setField(service, "bearerToken", BEARER_TOKEN);
    }

    @Test
    @DisplayName("EU Status can be enquired for accepted outcome and status api call is audited")
    void statusEnquiryForAcceptedOutcomeCanBeDone() {
        StatusResponse statusResponse = XmlTestMarshaller.unmarshall(response("status/accept.xml"), StatusResponse.class);
        when(gatewayService.get(eq(endpoint), eq(StatusResponse.class), eq(BEARER_TOKEN))).thenReturn(statusResponse);

        StatusResultResponse response = service.enquire(USER_ID);

        verify(adminAuditService, times(1)).save(STATUS, response.getOutcome(), USER_ID);

        assertAll("Status Enquiry Response",
                () -> assertThat(response.getOutcome(), equalTo(ACCEPTED)),
                () -> assertThat(response.getStatus(), equalTo(1)),
                () -> assertThat(response.getTotalItems(), equalTo(5000L)),
                () -> assertThat(response.getLastDisseminationDateTime(), notNullValue())
        );
    }

    @Test
    @DisplayName("EU Status can be enquired for rejected outcome and status api call is audited")
    void statusEnquiryForRejectedOutcomeCanBeDone() {
        StatusResponse statusResponse = XmlTestMarshaller.unmarshall(response("status/reject.xml"), StatusResponse.class);
        when(gatewayService.get(eq(endpoint), eq(StatusResponse.class), eq(BEARER_TOKEN))).thenReturn(statusResponse);

        StatusResultResponse response = service.enquire(USER_ID);

        verify(adminAuditService, times(1)).save(STATUS, response.getOutcome(), USER_ID);

        assertAll("Status Enquiry Response",
                () -> assertThat(response.getOutcome(), equalTo(REJECTED))
        );
    }
}
