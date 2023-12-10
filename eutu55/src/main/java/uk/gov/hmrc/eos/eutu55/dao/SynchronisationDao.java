package uk.gov.hmrc.eos.eutu55.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import uk.gov.hmrc.eos.eutu55.model.IossVat;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static uk.gov.hmrc.eos.eutu55.utils.DateUtil.toLocalDate;
import static uk.gov.hmrc.eos.eutu55.utils.DateUtil.toLocalDateTime;
import static uk.gov.hmrc.eos.eutu55.utils.DateUtil.toSqlDate;
import static uk.gov.hmrc.eos.eutu55.utils.DateUtil.toSqlTimestamp;

@Slf4j
@RequiredArgsConstructor
@Repository
public class SynchronisationDao {

    private final JdbcTemplate jdbcTemplate;

    private static final String INSERT_SQL = "insert into IOSS_VAT_TAB (ID, IOSS_VAT_ID, VALIDITY_START_DATE, " +
            "VALIDITY_END_DATE, CREATION_DATE, EU_MODIFICATION_DATE) values(IOSS_VAT_TAB_SEQ.nextval, ?, ?, ?, ?, ?)";

    private static final String UPDATE_SQL = "update IOSS_VAT_TAB set VALIDITY_END_DATE = ?, EU_MODIFICATION_DATE = ?, UPDATED_DATE = ? where IOSS_VAT_ID = ?";

    private static final String DELETE_SQL = "delete from IOSS_VAT_TAB where IOSS_VAT_ID = ?";

    private static final String SELECT_SQL = "SELECT * FROM IOSS_VAT_TAB where IOSS_VAT_ID = ?";

    private static final String SELECT_EXISTS_SQL = "SELECT count(IOSS_VAT_ID) FROM IOSS_VAT_TAB where IOSS_VAT_ID = ?";

    public void create(IossVat iossVat) {
        jdbcTemplate.update(INSERT_SQL,
                iossVat.getIossVatId(),
                toSqlDate(iossVat.getValidityStartDate() != null ? iossVat.getValidityStartDate(): LocalDate.now()),
                toSqlDate(iossVat.getValidityEndDate()),
                new Timestamp(System.currentTimeMillis()),
                toSqlTimestamp(iossVat.getEuModificationDateTime()));
        log.debug("Created Ioss Vat record {}", iossVat.getIossVatId());
    }

    public void update(IossVat iossVat) {
        jdbcTemplate.update(UPDATE_SQL,
                toSqlDate(iossVat.getValidityEndDate()),
                toSqlTimestamp(iossVat.getEuModificationDateTime()),
                Timestamp.valueOf(LocalDateTime.now()),
                iossVat.getIossVatId());
        log.debug("Updated Ioss Vat record {}", iossVat.getIossVatId());
    }

    public void delete(String iossVatId) {
        jdbcTemplate.update(DELETE_SQL, iossVatId);
        log.debug("Deleted Ioss Vat record {}", iossVatId);
    }

    public IossVat retrieve(String iossVatId) {
        try {
            return jdbcTemplate.queryForObject(SELECT_SQL,
                    (rs, rowNum) -> IossVat.builder()
                            .id(rs.getLong("ID"))
                            .iossVatId(rs.getString("IOSS_VAT_ID"))
                            .validityStartDate(toLocalDate(rs.getDate("VALIDITY_START_DATE")))
                            .validityEndDate(toLocalDate(rs.getDate("VALIDITY_END_DATE")))
                            .createdDateTime(toLocalDateTime(rs.getTimestamp("CREATION_DATE")))
                            .updatedDateTime(toLocalDateTime(rs.getTimestamp("UPDATED_DATE")))
                            .euModificationDateTime(toLocalDateTime(rs.getTimestamp("EU_MODIFICATION_DATE")))
                            .build(),
                    iossVatId);
        } catch (EmptyResultDataAccessException e) {
            log.info("Ioss Vat record {} not found in PDS", iossVatId);
            return null;
        }
    }

    public boolean iossVatExists(String iossVatId) {
        Integer count = jdbcTemplate.queryForObject(SELECT_EXISTS_SQL, Integer.class, iossVatId);
        return count != null && count > 0;
    }
}
