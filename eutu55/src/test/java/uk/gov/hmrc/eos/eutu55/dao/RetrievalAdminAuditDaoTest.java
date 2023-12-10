package uk.gov.hmrc.eos.eutu55.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Repository;
import uk.gov.hmrc.eos.eutu55.entity.AdminAudit;
import uk.gov.hmrc.eos.eutu55.repository.AdminAuditRepository;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static uk.gov.hmrc.eos.eutu55.utils.AdminActionType.RETRIEVAL;
import static uk.gov.hmrc.eos.eutu55.utils.Outcome.ACCEPTED;
import static uk.gov.hmrc.eos.eutu55.utils.Outcome.REJECTED;


@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Repository.class))
public class RetrievalAdminAuditDaoTest {
    private static final String USER_ID = "TEST_USER";

    private AdminAuditDao adminAuditDao;

    @Autowired
    private AdminAuditRepository adminAuditRepository;

    @Autowired
    public RetrievalAdminAuditDaoTest(AdminAuditDao adminAuditDao) {
        this.adminAuditDao = adminAuditDao;
    }

    @Test
    @DisplayName("Create AdminAuditTab record for outcome accepted is created successfully")
    void acceptedRetrievalRecordCreatedSuccessfully() {
        AdminAudit adminAudit = AdminAudit.buildAdminAudit(RETRIEVAL, ACCEPTED, USER_ID);
        AdminAudit result = adminAuditDao.save(adminAudit);
        Optional<AdminAudit> persistedRecord = adminAuditRepository.findById(result.getId());
        AdminAudit adminAuditTab = persistedRecord.orElseGet(AdminAudit::new);
        assertThat(result.getId(), equalTo(adminAuditTab.getId()));
        assertThat(result.getAction(), equalTo(RETRIEVAL));
        assertThat(adminAudit.getEuPayload(), is(nullValue()));
        assertThat(adminAudit.getRequestTime(), equalTo(adminAuditTab.getRequestTime()));
    }

    @Test
    @DisplayName("Create AdminAuditTab record for outcome rejected is created successfully")
    void rejectedRetrievalRecordCreatedSuccessfully() {
        AdminAudit adminAudit = AdminAudit.buildAdminAudit(RETRIEVAL, REJECTED, USER_ID);
        AdminAudit result = adminAuditDao.save(adminAudit);
        Optional<AdminAudit> persistedRecord = adminAuditRepository.findById(result.getId());
        AdminAudit adminAuditTab = persistedRecord.orElseGet(AdminAudit::new);
        assertThat(result.getId(), equalTo(adminAuditTab.getId()));
        assertThat(result.getAction(), equalTo(RETRIEVAL));
        assertThat(adminAudit.getEuPayload(), is(nullValue()));
        assertThat(adminAudit.getRequestTime(), equalTo(adminAuditTab.getRequestTime()));
    }

}
