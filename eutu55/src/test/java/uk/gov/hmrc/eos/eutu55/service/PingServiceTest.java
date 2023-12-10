package uk.gov.hmrc.eos.eutu55.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmrc.eos.eutu55.helper.TestHelper.response;
import static uk.gov.hmrc.eos.eutu55.utils.AdminActionType.PING;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmrc.eos.eutu55.helper.XmlTestMarshaller;
import uk.gov.hmrc.eos.eutu55.model.PingStatusResponse;
import uk.gov.hmrc.eos.eutu55.utils.Outcome;
import uk.gov.hmrc.eos.eutu55.utils.Status;
import uk.gov.hmrc.eu.eutu55.ping.PingResponse;

@ExtendWith(MockitoExtension.class)
class PingServiceTest {

    private String endpoint = "eis/endpoint";
    private static final String BEARER_TOKEN = "f916fa9b-0500-3837-a321-4c76f67499e0";
    private static final String USER_ID = "TEST_USER";

    @Mock
    private AdminAuditService adminAuditService;
    @Mock
    private GatewayService gatewayService;

    @InjectMocks
    private PingService service;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(service, "endpoint", endpoint);
        ReflectionTestUtils.setField(service, "bearerToken", BEARER_TOKEN);
    }

    @Test
    @DisplayName("EU App Health can be pinged for up status and ping api call is audited")
    void pingForUp() {
        PingResponse pingResponse = XmlTestMarshaller.unmarshall(response("ping/up.xml"), PingResponse.class);
        when(gatewayService.get(eq(endpoint), eq(PingResponse.class), eq(BEARER_TOKEN))).thenReturn(pingResponse);

        PingStatusResponse response = service.ping(USER_ID);

        verify(adminAuditService, times(1)).save(PING, Outcome.valueOf(response.getStatus().getValue()), USER_ID);

        assertAll("Ping Response",
                () -> assertThat(response.getStatus(), equalTo(Status.UP)));
    }

    @Test
    @DisplayName("EU App Health can be pinged for down status and ping api call is audited")
    void pingForDown() {
        PingResponse pingResponse = XmlTestMarshaller.unmarshall(response("ping/down.xml"), PingResponse.class);
        when(gatewayService.get(eq(endpoint), eq(PingResponse.class), eq(BEARER_TOKEN))).thenReturn(pingResponse);

        PingStatusResponse response = service.ping(USER_ID);

        verify(adminAuditService, times(1)).save(PING, Outcome.valueOf(response.getStatus().getValue()), USER_ID);

        assertAll("Ping Response",
                () -> assertThat(response.getStatus(), equalTo(Status.DOWN))
        );
    }
}
