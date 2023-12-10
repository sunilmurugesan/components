package uk.gov.hmrc.eos.eutu55.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmrc.eos.eutu55.entity.AdminAudit;
import uk.gov.hmrc.eos.eutu55.entity.SubscriptionStatus;
import uk.gov.hmrc.eos.eutu55.model.SubscriptionRequest;
import uk.gov.hmrc.eos.eutu55.repository.AdminAuditRepository;
import uk.gov.hmrc.eos.eutu55.repository.SubscriptionStatusRepository;

import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmrc.eos.eutu55.config.RequestCorrelationId.X_CORRELATION_ID;
import static uk.gov.hmrc.eos.eutu55.service.SubscriptionStatusService.SUBSCRIPTION_STATUS_ID;
import static uk.gov.hmrc.eos.eutu55.utils.AdminActionType.SUBSCRIPTION;
import static uk.gov.hmrc.eos.eutu55.utils.ErrorCode.EUTU55_EIS_NO_RESPONSE_BODY;
import static uk.gov.hmrc.eos.eutu55.utils.Outcome.ACCEPTED;
import static uk.gov.hmrc.eos.eutu55.utils.Outcome.REJECTED;
import static uk.gov.hmrc.eos.eutu55.utils.SubscriptionStatusType.OFF;
import static uk.gov.hmrc.eos.eutu55.utils.SubscriptionStatusType.ON;

@SpringBootTest
@AutoConfigureMockMvc
public class SubscriptionIT {

    private static final String SUBSCRIPTION_POST_URL = "/pds/cnit/eutu55/subscription/v1";

    private final MockMvc mockMvc;
    private final SubscriptionStatusRepository subscriptionStatusRepository;
    private final AdminAuditRepository adminAuditRepository;
    private static final String USER_ID = "TEST_USER";

    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().notifier(new Slf4jNotifier(true)))
            .build();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("wiremock.test.baseurl", wireMockServer::baseUrl);
    }

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    public SubscriptionIT(final MockMvc mockMvc,
                          final SubscriptionStatusRepository subscriptionStatusRepository,
                          final AdminAuditRepository adminAuditRepository) {
        this.mockMvc = mockMvc;
        this.subscriptionStatusRepository = subscriptionStatusRepository;
        this.adminAuditRepository = adminAuditRepository;
    }

    @Test
    @DisplayName("Given a valid json request payload is received, the payload is transformed into xml message" +
            "When EIS is called by sending the transformed xml payload" +
            "And EIS returns an accepted outcome in the response " +
            "Then Subscription API call is audited with correct outcome from EIS " +
            "And the Subscription status is updated to correct status in PDS " +
            "And the outcome from EIS is converted to a json subscription response " +
            "And the subscription response is returned")
    void processUpdateSubscriptionStatus_Accepted() throws Exception {

        setupEISSubscriptionStub(HttpStatus.OK, "accept.xml");

        SubscriptionRequest request = new SubscriptionRequest(ON, "test@hmrc.gov.uk");
        mockMvc.perform(post(SUBSCRIPTION_POST_URL)
                        .content(mapper.writeValueAsString(request))
                        .contentType(APPLICATION_JSON)
                        .header("userId", USER_ID)
                        .header(X_CORRELATION_ID, "f7b74594-b6a7-45e6-a69c-b2563381aed9"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.outcome", equalTo(ACCEPTED.name())));

        Optional<SubscriptionStatus> subscriptionStatus = subscriptionStatusRepository.findById(SUBSCRIPTION_STATUS_ID);
        assertThat(subscriptionStatus.orElseThrow().getStatus(), equalTo(ON));

        Optional<AdminAudit> adminAudit = adminAuditRepository.findOne((root, query, cb) -> cb.equal(root.get("action"), SUBSCRIPTION));
        assertThat(adminAudit.orElseThrow().getResponseOutcome(), equalTo(ACCEPTED));
    }

    @Test
    @DisplayName("Given a subscription status update is received " +
            "When a blank email supplied in the request payload " +
            "Then the request is rejected")
    void rejectWhenBlankEmailSuppliedInRequestPayload() throws Exception {

        SubscriptionRequest request = new SubscriptionRequest(ON, "");
        mockMvc.perform(post(SUBSCRIPTION_POST_URL)
                        .content(mapper.writeValueAsString(request))
                        .contentType(APPLICATION_JSON)
                        .header("userId", USER_ID))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Given a subscription status update is received " +
            "When an invalid email supplied in the request payload " +
            "Then the request is rejected")
    void rejectWhenInvalidEmailSuppliedInRequestPayload() throws Exception {

        SubscriptionRequest request = new SubscriptionRequest(ON, "invalid-email.id.com");
        mockMvc.perform(post(SUBSCRIPTION_POST_URL)
                        .content(mapper.writeValueAsString(request))
                        .contentType(APPLICATION_JSON)
                        .header("userId", USER_ID))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Given a valid json request payload is received, the payload is transformed into xml message" +
            "When EIS is called by sending the transformed xml payload" +
            "And EIS returns an rejected outcome in the response " +
            "Then Subscription API call is audited with correct outcome from EIS " +
            "And the Subscription status is not updated in PDS " +
            "And the outcome from EIS is converted to a json subscription response " +
            "And the subscription response is returned")
    void processUpdateSubscriptionStatus_Rejected() throws Exception {

        setupEISSubscriptionStub(HttpStatus.OK, "reject.xml");

        SubscriptionRequest request = new SubscriptionRequest(OFF, "test@hmrc.gov.uk");
        mockMvc.perform(post(SUBSCRIPTION_POST_URL)
                        .content(mapper.writeValueAsString(request))
                        .contentType(APPLICATION_JSON)
                        .header("userId", USER_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.outcome", equalTo(REJECTED.name())));

        Optional<SubscriptionStatus> subscriptionStatus = subscriptionStatusRepository.findById(SUBSCRIPTION_STATUS_ID);
        assertThat(subscriptionStatus.isEmpty(), equalTo(Boolean.TRUE));

        Optional<AdminAudit> adminAudit = adminAuditRepository.findOne((root, query, cb) -> cb.equal(root.get("action"), SUBSCRIPTION));
        assertThat(adminAudit.orElseThrow().getResponseOutcome(), equalTo(REJECTED));
    }

    @Test
    @DisplayName("Given a valid json request payload is received and the payload is transformed into xml message" +
            "When EIS is called by sending the transformed xml payload" +
            "And EIS returns an empty response " +
            "Then Subscription API call is audited with correct outcome from EIS " +
            "And an Error is returned")
    void errorWhenEmptyResponseReturnedByEIS() throws Exception {
        setupEISSubscriptionStub(HttpStatus.OK, null);

        SubscriptionRequest request = new SubscriptionRequest(ON, "test@hmrc.gov.uk");
        mockMvc.perform(post(SUBSCRIPTION_POST_URL)
                        .content(mapper.writeValueAsString(request))
                        .contentType(APPLICATION_JSON)
                        .header("userId", USER_ID))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.code", equalTo(EUTU55_EIS_NO_RESPONSE_BODY.code())));
    }

    private void setupEISSubscriptionStub(HttpStatus expectedHttpStatus, String subscriptionEISResponseFilename) {
        ResponseDefinitionBuilder responseBuilder = aResponse().withStatus(expectedHttpStatus.value())
                .withHeader("Content-Type", MediaType.APPLICATION_XML_VALUE);
        if (subscriptionEISResponseFilename != null) {
            responseBuilder.withBodyFile(String.format("response/subscription/%s", subscriptionEISResponseFilename));
        } else {
            responseBuilder.withBody("");
        }
        wireMockServer.stubFor(WireMock.post(urlEqualTo("/eis/cnit/eutu55/subscription"))
                .willReturn(responseBuilder));
    }

    @AfterEach
    void tearDown() {
        adminAuditRepository.deleteAll();
    }
}
