package uk.gov.hmrc.eos.eutu55.integration;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmrc.eos.eutu55.utils.Outcome.ACCEPTED;
import static uk.gov.hmrc.eos.eutu55.utils.Outcome.REJECTED;

@SpringBootTest
@AutoConfigureMockMvc
class StatusIT {

    private static final String STATUS_GET_URL = "/pds/cnit/eutu55/status/v1";
    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().notifier(new Slf4jNotifier(true)))
            .build();
    private final MockMvc mockMvc;
    private static final String USER_ID = "TEST_USER";

    @Autowired
    public StatusIT(final MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("wiremock.test.baseurl", wireMockServer::baseUrl);
    }

    @Test
    @DisplayName("When status enquiry is made to EIS then PDS receives the ACCEPTED response and the response outcome should be ACCEPTED")
    void verifyAcceptedResponseOutcome() throws Exception {

        setupEisStatusStub("accept.xml");

        mockMvc.perform(get(STATUS_GET_URL)
                .contentType(APPLICATION_JSON)
                .header("userId", USER_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.outcome", equalTo(ACCEPTED.name())))
                .andExpect(jsonPath("$.status", equalTo(1)))
                .andExpect(jsonPath("$.totalItems", equalTo(214748364)));
    }

    @Test
    @DisplayName("When status enquiry is made to EIS then PDS receives the REJECTED response and the response outcome should be REJECTED")
    void verifyRejectedResponseOutcome() throws Exception {

        setupEisStatusStub("reject.xml");

        mockMvc.perform(get(STATUS_GET_URL)
                .contentType(APPLICATION_JSON)
                .header("userId", USER_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.outcome", equalTo(REJECTED.name())));
    }

    private void setupEisStatusStub(String retrievalBodyFilename) {
        wireMockServer.stubFor(WireMock.get(urlEqualTo("/eis/cnit/eutu55/status"))
                .willReturn(aResponse()
                        .withStatus(HttpStatus.OK.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_XML_VALUE)
                        .withBodyFile(String.format("response/status/%s", retrievalBodyFilename))));
    }

}
