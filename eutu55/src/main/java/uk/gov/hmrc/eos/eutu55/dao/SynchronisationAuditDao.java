package uk.gov.hmrc.eos.eutu55.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import uk.gov.hmrc.eos.eutu55.model.IossVatAudit;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmrc.eos.eutu55.utils.DateUtil.toSqlDate;
import static uk.gov.hmrc.eos.eutu55.utils.DateUtil.toSqlTimestamp;

@Slf4j
@RequiredArgsConstructor
@Repository
public class SynchronisationAuditDao {

    private static final String INSERT_SQL = "insert into IOSS_VAT_AUDIT_TAB (IOSS_VAT_ID, VALIDITY_START_DATE, " +
            "VALIDITY_END_DATE, EU_MODIFICATION_DATE, OPERATION, OUTCOME, AUDIT_DATE, REMARKS) values(?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SELECT_BY_AUDIT_DATE_SQL = "select IOSS_VAT_ID from IOSS_VAT_AUDIT_TAB where AUDIT_DATE < ?";
    private static final String DELETE_BY_AUDIT_DATE_SQL = "delete from IOSS_VAT_AUDIT_TAB where AUDIT_DATE < ?";

    @Value("${pds.eutu55.jdbc.batch_size: 500}")
    private int batchSize;

    private final JdbcTemplate jdbcTemplate;

    public void save(List<IossVatAudit> records) {
        int[][] insertCounts = jdbcTemplate.batchUpdate(INSERT_SQL,
                records,
                batchSize,
                (ps, record) -> {
                    ps.setString(1, record.getIossVat().getIossVatId());
                    ps.setDate(2, toSqlDate(record.getIossVat().getValidityStartDate()));
                    ps.setDate(3, toSqlDate(record.getIossVat().getValidityEndDate()));
                    ps.setTimestamp(4, toSqlTimestamp(record.getIossVat().getEuModificationDateTime()));
                    ps.setString(5, record.getOperationType().operation());
                    ps.setInt(6, record.getOutcome().getValue());
                    ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
                    ps.setString(8, record.getRemarks());
                });
        log.info("Created {} IOSS VAT audit records in {} batch(es)", records.size(), insertCounts.length);
    }

    public void deleteByAuditDate(LocalDateTime auditTime) {
        log.info("Deleting Ioss Vat audit records which are older than {}", auditTime);
        if (auditTime != null) {
            Object[] auditTimeArgs = new Object[]{Timestamp.valueOf(auditTime)};
            List<String> iossVatIds = jdbcTemplate.queryForList(SELECT_BY_AUDIT_DATE_SQL, String.class, auditTimeArgs);
            // This is required to avoid SQL Warning Code: -1100, SQLState: 02000
            if (!iossVatIds.isEmpty()) {
                log.info("Deleting records {} from IOSS_VAT_AUDIT_TAB table", iossVatIds);
                int deleteCount = jdbcTemplate.update(DELETE_BY_AUDIT_DATE_SQL, auditTimeArgs);
                log.info("Deleted {} Ioss Vat audit records", deleteCount);
            }
        }
    }
}
