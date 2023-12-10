package uk.gov.hmrc.eos.eutu55.dao;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmrc.eos.eutu55.model.IossVatAudit;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static uk.gov.hmrc.eos.eutu55.IossVatTestHelper.iossVatAuditCreateRecordOf;
import static uk.gov.hmrc.eos.eutu55.IossVatTestHelper.iossVatAuditCreateRejectRecordOf;
import static uk.gov.hmrc.eos.eutu55.IossVatTestHelper.iossVatAuditDeleteRecordOf;
import static uk.gov.hmrc.eos.eutu55.IossVatTestHelper.iossVatAuditUpdateRecordOf;

@DataJdbcTest
class SynchronisationAuditDaoTest {

    private JdbcTemplate jdbcTemplate;
    private SynchronisationAuditDao dao;
    private static final String TABLE_IOSS_VAT_AUDIT_TAB = "IOSS_VAT_AUDIT_TAB";
    private static final String ACCEPT_SELECT_SQL = "select IOSS_VAT_ID from IOSS_VAT_AUDIT_TAB WHERE OUTCOME=1";
    private static final String REJECT_SELECT_SQL = "select IOSS_VAT_ID from IOSS_VAT_AUDIT_TAB WHERE OUTCOME=0";
    private static final String SELECT_SQL = "select IOSS_VAT_ID from IOSS_VAT_AUDIT_TAB";
    private static final String UPDATE_SQL = "UPDATE IOSS_VAT_AUDIT_TAB SET AUDIT_DATE = ? WHERE IOSS_VAT_ID = ?";

    @Autowired
    public SynchronisationAuditDaoTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.dao = new SynchronisationAuditDao(jdbcTemplate);
        ReflectionTestUtils.setField(this.dao, "batchSize", 5);
    }

    @Test
    @DisplayName("Ioss VAT audit records should be persisted successfully in PDS")
    void iossVatAuditRecordsCanBePersistedSuccessfully() {
        List<IossVatAudit> records = List.of(iossVatAuditCreateRecordOf("IM0000000001"),
                iossVatAuditUpdateRecordOf("IM0000000002"),
                iossVatAuditDeleteRecordOf("IM0000000003"),
                iossVatAuditCreateRejectRecordOf("IM0000000004"));
        dao.save(records);
        int count = JdbcTestUtils.countRowsInTable(jdbcTemplate, TABLE_IOSS_VAT_AUDIT_TAB);
        assertThat(count, is(records.size()));

        List<String> iossVatIds = jdbcTemplate.queryForList(ACCEPT_SELECT_SQL, String.class);
        assertThat(iossVatIds, Matchers.<Collection<String>>allOf(
                hasSize(is(3)),
                hasItems("IM0000000001", "IM0000000002", "IM0000000003")));

        iossVatIds = jdbcTemplate.queryForList(REJECT_SELECT_SQL, String.class);
        assertThat(iossVatIds, Matchers.<Collection<String>>allOf(
                hasSize(is(1)),
                hasItems("IM0000000004")));
    }

    @Test
    @DisplayName("Ioss VAT audit records can be deleted based on supplied audit date")
    void iossVatAuditRecordsCanBeDeletedByAuditDate() {
        List<IossVatAudit> records = List.of(iossVatAuditCreateRecordOf("IM0000000001"),
                iossVatAuditUpdateRecordOf("IM0000000002"),
                iossVatAuditDeleteRecordOf("IM0000000003"),
                iossVatAuditCreateRejectRecordOf("IM0000000004"));
        dao.save(records);

        jdbcTemplate.update(UPDATE_SQL, Timestamp.valueOf("2023-02-07 10:10:10.0"), "IM0000000001");
        jdbcTemplate.update(UPDATE_SQL, Timestamp.valueOf(LocalDateTime.now()), "IM0000000002");
        jdbcTemplate.update(UPDATE_SQL, Timestamp.valueOf("2023-02-06 00:00:01.0"), "IM0000000003");
        jdbcTemplate.update(UPDATE_SQL, Timestamp.valueOf("2021-01-01 00:00:10.0"), "IM0000000004");

        int count = JdbcTestUtils.countRowsInTable(jdbcTemplate, TABLE_IOSS_VAT_AUDIT_TAB);
        assertThat(count, is(records.size()));



        LocalDateTime auditDateForDeletion = Timestamp.valueOf("2023-02-06 00:00:00.0").toLocalDateTime();

        dao.deleteByAuditDate(auditDateForDeletion);

        List<String> iossVatIds = jdbcTemplate.queryForList(SELECT_SQL, String.class);
        assertThat(iossVatIds, Matchers.<Collection<String>>allOf(
                hasSize(is(3)),
                hasItems("IM0000000001", "IM0000000002", "IM0000000003")));
    }
}