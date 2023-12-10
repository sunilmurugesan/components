package uk.gov.hmrc.eos.eutu55.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmrc.eos.eutu55.entity.AdminAudit;
import uk.gov.hmrc.eos.eutu55.entity.SubscriptionStatus;
import uk.gov.hmrc.eos.eutu55.model.RetrievalRequest;
import uk.gov.hmrc.eos.eutu55.repository.AdminAuditRepository;
import uk.gov.hmrc.eos.eutu55.repository.SubscriptionStatusRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Iterator;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmrc.eos.eutu55.utils.Outcome.ACCEPTED;
import static uk.gov.hmrc.eos.eutu55.utils.Outcome.REJECTED;
import static uk.gov.hmrc.eos.eutu55.utils.SubscriptionStatusType.ON;
import static uk.gov.hmrc.eos.eutu55.utils.SubscriptionStatusType.RECOVERY;

@SpringBootTest
@AutoConfigureMockMvc
public class RetrievalIT extends AbstractIT {

    private static final String POST_URL = "/pds/cnit/eutu55/retrieval/v1";

    private final MockMvc mockMvc;

    @Autowired
    private SubscriptionStatusRepository subscriptionStatusRepository;

    @Autowired
    private AdminAuditRepository adminAuditRepository;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    public RetrievalIT(final MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    private static final String USER_ID = "TEST_USER";

    @Test
    @DisplayName("Given a valid json request payload is received, the payload is transformed into xml message" +
            "When EIS is called by sending the transformed xml payload" +
            "And EIS returns an accepted outcome in the response " +
            "Then a record is created in Admin Audit with the correct outcome " +
            "And the subscription status is changed to RECOVERY")
    void processSuccessRetrieval() throws Exception {

        setupEISRetrievalStub(HttpStatus.OK, "accept.xml");

        RetrievalRequest request = new RetrievalRequest(LocalDate.now());
        mockMvc.perform(post(POST_URL)
                        .content(mapper.writeValueAsString(request))
                        .contentType(APPLICATION_JSON)
                .header("userId", USER_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json")
                )
                .andExpect(jsonPath("$.outcome", equalTo(ACCEPTED.name())));

        Iterator<AdminAudit> auditIterator = adminAuditRepository.findAll().iterator();
        assertThat(auditIterator.hasNext(), is(true));
        AdminAudit audit = auditIterator.next();
        assertThat(audit.getResponseOutcome(), is(ACCEPTED));
        assertThat(audit.getEuPayload(), is(nullValue()));

        Iterator<SubscriptionStatus> statusIterator = subscriptionStatusRepository.findAll().iterator();
        assertThat(statusIterator.hasNext(), is(true));
        SubscriptionStatus status = statusIterator.next();
        assertThat(status.getStatus(), equalTo(RECOVERY));
    }

    @Test
    @DisplayName("Given a valid json request payload is received, the payload is transformed into xml message" +
            "When EIS is called by sending the transformed xml payload" +
            "And EIS returns a rejected outcome in the response " +
            "Then a record is created in Admin Audit with the correct outcome " +
            "And the subscription status is not changed")
    void processRejectRetrieval() throws Exception {

        setupEISRetrievalStub(HttpStatus.OK, "reject.xml");

        SubscriptionStatus statusRecord = SubscriptionStatus.builder().id(1)
                .status(ON).requestTime(LocalDateTime.now()).build();
        subscriptionStatusRepository.save(statusRecord);

        int year = 2019;
        int month = 1;
        int day = 20;
        RetrievalRequest request = new RetrievalRequest(LocalDate.of(year, month, day));
        mockMvc.perform(post(POST_URL)
                        .content(mapper.writeValueAsString(request))
                        .contentType(APPLICATION_JSON)
                .header("userId", USER_ID))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.outcome", equalTo(REJECTED.name())));

        Iterator<AdminAudit> auditIterator = adminAuditRepository.findAll().iterator();
        assertThat(auditIterator.hasNext(), is(true));
        AdminAudit audit = auditIterator.next();
        assertThat(audit.getResponseOutcome(), is(REJECTED));
        assertThat(audit.getEuPayload(), is(nullValue()));

        //No changes to status record
        Iterator<SubscriptionStatus> statusIterator = subscriptionStatusRepository.findAll().iterator();
        SubscriptionStatus status = statusIterator.next();
        assertThat(status.getStatus(), equalTo(ON));
    }

    private void setupEISRetrievalStub(HttpStatus expectedHttpStatus, String retrievalBodyFilename) {
        wireMockServer.stubFor(WireMock.post(urlEqualTo("/eis/cnit/eutu55/retrieval"))
                .willReturn(aResponse()
                        .withStatus(expectedHttpStatus.value())
                        .withHeader("Content-Type", MediaType.APPLICATION_XML_VALUE)
                        .withBodyFile(String.format("response/retrieval/%s", retrievalBodyFilename))));
    }

    @AfterEach
    void tearDown() {
        adminAuditRepository.deleteAll();
        subscriptionStatusRepository.deleteAll();
    }

}
