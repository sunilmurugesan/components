package uk.gov.hmrc.eos.eutu55.integration;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmrc.eos.eutu55.entity.AdminAudit;
import uk.gov.hmrc.eos.eutu55.entity.SubscriptionStatus;
import uk.gov.hmrc.eos.eutu55.helper.TestHelper;
import uk.gov.hmrc.eos.eutu55.repository.AdminAuditRepository;
import uk.gov.hmrc.eos.eutu55.repository.SubscriptionStatusRepository;

import java.time.LocalDateTime;
import java.util.Iterator;

import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmrc.eos.eutu55.config.RequestCorrelationId.X_CORRELATION_ID;
import static uk.gov.hmrc.eos.eutu55.utils.Outcome.ACCEPTED;
import static uk.gov.hmrc.eos.eutu55.utils.Outcome.REJECTED;
import static uk.gov.hmrc.eos.eutu55.utils.SubscriptionStatusType.OFF;
import static uk.gov.hmrc.eos.eutu55.utils.SubscriptionStatusType.ON;
import static uk.gov.hmrc.eos.eutu55.utils.SubscriptionStatusType.RECOVERY;

@SpringBootTest
@AutoConfigureMockMvc
public class NotificationIT {

    private static final String POST_URL = "/pds/cnit/eutu55/notificationcbs/v1";

    private final MockMvc mockMvc;

    @Autowired
    private SubscriptionStatusRepository subscriptionStatusRepository;

    @Autowired
    private AdminAuditRepository adminAuditRepository;

    @Autowired
    public NotificationIT(final MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    @DisplayName("When a valid success request payload is received then record created successfully in to admin audit and status is updated to ON")
    void processSuccessNotification() throws Exception {
        // JSON strings to check for in EU_PAYLOAD. Note: Fractions of seconds (milliseconds) are only output if not zero
        final String EU_PAYLOAD_TEXT_1 = "\"totalItems\": 100,";
        final String EU_PAYLOAD_TEXT_2 = "\"lastDisseminationDateTime\": \"2006-05-04T18:13:51\"";
        adminAuditRepository.deleteAll();
        mockMvc.perform(post(POST_URL)
                        .content(request("success-notification.xml"))
                        .contentType(APPLICATION_XML)
                        .header(X_CORRELATION_ID, "f7b74594-b6a7-45e6-a69c-b2563381aed9"))
                .andExpect(status().isOk())
                .andExpect(header().string(X_CORRELATION_ID, "f7b74594-b6a7-45e6-a69c-b2563381aed9"))
                .andExpect(content().xml(response("success.xml")));

        Iterator<AdminAudit> auditIterator = adminAuditRepository.findAll().iterator();
        assertThat(auditIterator.hasNext(), is(true));
        AdminAudit audit = auditIterator.next();
        assertThat(audit.getResponseOutcome(), is(ACCEPTED));
        assertThat(audit.getUserId(), is(SPACE));
        assertThat(StringUtils.deleteWhitespace(audit.getEuPayload()), equalTo(StringUtils.deleteWhitespace("{" + EU_PAYLOAD_TEXT_1 + EU_PAYLOAD_TEXT_2 + "}")));
        assertThat(audit.getEuPayload(), containsString("lastDisseminationDateTime"));

        Iterator<SubscriptionStatus> statusIterator = subscriptionStatusRepository.findAll().iterator();
        assertThat(statusIterator.hasNext(), is(true));
        SubscriptionStatus status = statusIterator.next();
        assertThat(status.getStatus(), equalTo(ON));

    }


    @Test
    @DisplayName("When a valid fail request payload is received then record created successfully in to admin audit and no change to status RECOVERY")
    void processFailNotification() throws Exception {
        adminAuditRepository.deleteAll();
        SubscriptionStatus statusRecord = SubscriptionStatus.builder().id(1)
                .status(RECOVERY).requestTime(LocalDateTime.now()).build();
        subscriptionStatusRepository.save(statusRecord);
        mockMvc.perform(post(POST_URL)
                        .content(request("fail-notification.xml"))
                        .contentType(APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(content().xml(response("success.xml")));

        Iterator<AdminAudit> auditIterator = adminAuditRepository.findAll().iterator();
        assertThat(auditIterator.hasNext(), is(true));
        AdminAudit audit = auditIterator.next();
        assertThat(audit.getResponseOutcome(), is(REJECTED));
        assertThat(audit.getUserId(), is(SPACE));

        Iterator<SubscriptionStatus> statusIterator = subscriptionStatusRepository.findAll().iterator();
        assertThat(statusIterator.hasNext(), is(true));
        SubscriptionStatus status = statusIterator.next();
        assertThat(status.getStatus(), equalTo(OFF));

    }

    @Test
    @DisplayName("When an invalid request payload is received then system should return a bad request")
    void processFaultRequestNotification() throws Exception {
        mockMvc.perform(post(POST_URL)
                        .content(request("fault-notification.xml"))
                        .contentType(APPLICATION_XML))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(response("fault-invalid-response.xml")));
    }

    private String request(String filename) {
        return TestHelper.request("notification/" + filename);
    }

    private String response(String filename) {
        return TestHelper.response("notification/" + filename);
    }

    @AfterEach
    void tearDown() {
        adminAuditRepository.deleteAll();
        subscriptionStatusRepository.deleteAll();
    }

}
