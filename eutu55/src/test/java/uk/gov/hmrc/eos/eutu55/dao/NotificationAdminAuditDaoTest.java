package uk.gov.hmrc.eos.eutu55.dao;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Repository;
import uk.gov.hmrc.eos.eutu55.entity.AdminAudit;
import uk.gov.hmrc.eos.eutu55.repository.AdminAuditRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static uk.gov.hmrc.eos.eutu55.utils.AdminActionType.RETRIEVAL;
import static uk.gov.hmrc.eos.eutu55.utils.Outcome.ACCEPTED;


@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Repository.class))
public class NotificationAdminAuditDaoTest {

    private AdminAuditDao adminAuditDao;

    @Autowired
    private AdminAuditRepository adminAuditRepository;

    @Autowired
    public NotificationAdminAuditDaoTest(AdminAuditDao adminAuditDao) {
        this.adminAuditDao = adminAuditDao;
    }

    @Test
    @DisplayName("Create AdminAuditTab record for outcome accepted is created successfully")
    void acceptedNotificationRecordCreatedSuccessfully() throws IOException {
        final String EU_PAYLOAD_TEXT_1 = "\"totalItems\": 100,";
        final String EU_PAYLOAD_TEXT_2 = "\"lastDisseminationDateTime\": \"2006-05-04T18:13:51.0\"";
        String notificationJsonString = new String(Files.readAllBytes(Paths.get("src/test/resources/payload/request/notification/success-notification-extract.json")));
        AdminAudit adminAudit = AdminAudit.buildAdminAuditForInboundCall(RETRIEVAL, ACCEPTED, notificationJsonString);
        AdminAudit result = adminAuditDao.save(adminAudit);
        Optional<AdminAudit> persistedRecord = adminAuditRepository.findById(result.getId());
        AdminAudit adminAuditTab = persistedRecord.orElseGet(AdminAudit::new);
        assertThat(result.getId(), equalTo(adminAuditTab.getId()));
        assertThat(result.getAction(), equalTo(RETRIEVAL));
        assertThat(StringUtils.deleteWhitespace(adminAudit.getEuPayload()), equalTo(StringUtils.deleteWhitespace("{" + EU_PAYLOAD_TEXT_1 + EU_PAYLOAD_TEXT_2 + "}")));
        assertThat(adminAudit.getRequestTime(), equalTo(adminAuditTab.getRequestTime()));
    }

}
