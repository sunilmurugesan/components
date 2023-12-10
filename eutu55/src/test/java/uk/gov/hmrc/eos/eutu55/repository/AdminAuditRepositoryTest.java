package uk.gov.hmrc.eos.eutu55.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmrc.eos.eutu55.entity.AdminAudit;
import uk.gov.hmrc.eos.eutu55.utils.AdminActionType;
import uk.gov.hmrc.eos.eutu55.utils.Outcome;

import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.SPACE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static uk.gov.hmrc.eos.eutu55.utils.AdminActionType.NOTIFICATION;
import static uk.gov.hmrc.eos.eutu55.utils.Outcome.ACCEPTED;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public class AdminAuditRepositoryTest {

    private AdminAuditRepository adminAuditRepository;
    private static final String USER_ID = "TEST_USER";


    @Autowired
    public AdminAuditRepositoryTest(AdminAuditRepository adminAuditRepository) {
        this.adminAuditRepository = adminAuditRepository;
    }

    @ParameterizedTest
    @CsvSource(
            {
                    "RETRIEVAL, ACCEPTED",
                    "SUBSCRIPTION, REJECTED",
                    "PING, ACCEPTED",
                    "STATUS, REJECTED",
                    "NOTIFICATION, ACCEPTED"
            })
    @DisplayName("Create AdminAuditTab records for each admin action with outcome successfully")
    void adminAuditTabRecordCreatedSuccessfully(String action, String outcome) {
        AdminAudit adminAudit = getAdminAuditTab(AdminActionType.valueOf(action), Outcome.valueOf(outcome));
        AdminAudit result = adminAuditRepository.save(adminAudit);
        Optional<AdminAudit> persistedRecord = adminAuditRepository.findById(result.getId());
        AdminAudit adminAuditTab = persistedRecord.get();
        assertThat(result.getId(), equalTo(adminAuditTab.getId()));
        assertThat(result.getAction(), equalTo(adminAuditTab.getAction()));
        assertThat(adminAudit.getResponseOutcome(), equalTo(adminAuditTab.getResponseOutcome()));
        assertThat(adminAudit.getRequestTime(), is(notNullValue()));
        assertThat(adminAudit.getRequestTime(), equalTo(adminAuditTab.getRequestTime()));
        assertThat(adminAudit.getEuPayload(), is(nullValue()));
    }

    @Test
    @DisplayName("Admin audit record can be created with empty user id for inbound EIS call")
    void adminAuditTabRecordCreatedSuccessfullyWithoutUserid() {
        String payload = "payload";
        AdminAudit adminAudit = AdminAudit.buildAdminAuditForInboundCall(NOTIFICATION, ACCEPTED, "payload");
        AdminAudit result = adminAuditRepository.save(adminAudit);
        Optional<AdminAudit> persistedRecord = adminAuditRepository.findById(result.getId());
        AdminAudit adminAuditTab = persistedRecord.get();
        assertThat(result.getId(), equalTo(adminAuditTab.getId()));
        assertThat(result.getAction(), equalTo(adminAuditTab.getAction()));
        assertThat(adminAudit.getResponseOutcome(), equalTo(adminAuditTab.getResponseOutcome()));
        assertThat(adminAudit.getRequestTime(), equalTo(adminAuditTab.getRequestTime()));
        assertThat(adminAudit.getUserId(), is(SPACE));
        assertThat(adminAudit.getEuPayload(), equalTo(payload));
    }

    private AdminAudit getAdminAuditTab(AdminActionType action, Outcome outcome) {
       return AdminAudit.buildAdminAudit(action, outcome, USER_ID);
    }
}
