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
import static uk.gov.hmrc.eos.eutu55.config.RequestCorrelationId.X_CORRELATION_ID;
import static uk.gov.hmrc.eos.eutu55.utils.Status.DOWN;
import static uk.gov.hmrc.eos.eutu55.utils.Status.UP;

@SpringBootTest
@AutoConfigureMockMvc
class PingIT {

    private static final String PING_URL = "/pds/cnit/eutu55/ping/v1";
    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().notifier(new Slf4jNotifier(true)))
            .build();
    private final MockMvc mockMvc;
    private static final String USER_ID = "TEST_USER";

    @Autowired
    public PingIT(final MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("wiremock.test.baseurl", wireMockServer::baseUrl);
    }

    @Test
    @DisplayName("When ping request is made with EIS and the EIS returns the status as UP then the status is returned")
    void pingReturnsUpStatus() throws Exception {

        setupEisStatusStub("up.xml");

        mockMvc.perform(get(PING_URL)
                        .contentType(APPLICATION_JSON)
                        .header("userId", USER_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.status", equalTo(UP.name())));


    }

        @Test
        @DisplayName("When ping request is made with EIS and the EIS returns the status as DOWN then the status is returned")
        void pingReturnsDownStatus () throws Exception {

            setupEisStatusStub("down.xml");

            mockMvc.perform(get(PING_URL)
                            .contentType(APPLICATION_JSON)
                            .header("userId", USER_ID))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json"))
                    .andExpect(jsonPath("$.status", equalTo(DOWN.name())));
        }

        private void setupEisStatusStub (String pingBodyFilename){
            wireMockServer.stubFor(WireMock.get(urlEqualTo("/eis/cnit/eutu55/ping"))
                    .willReturn(aResponse()
                            .withStatus(HttpStatus.OK.value())
                            .withHeader("Content-Type", MediaType.APPLICATION_XML_VALUE)
                            .withHeader(X_CORRELATION_ID, "f7b74594-b6a7-45e6-a69c-b2563381aed9")
                            .withBodyFile(String.format("response/ping/%s", pingBodyFilename))));
        }
    }
