package uk.gov.hmrc.eos.eutu55.dao;

import org.exparity.hamcrest.date.LocalDateTimeMatchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.jdbc.JdbcTestUtils;
import uk.gov.hmrc.eos.eutu55.model.IossVat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertAll;
import static uk.gov.hmrc.eos.eutu55.IossVatTestHelper.iossVatCreateOf;
import static uk.gov.hmrc.eos.eutu55.IossVatTestHelper.iossVatDeleteOf;

@DataJdbcTest
public class SynchronisationDaoTest {

    private static final String TABLE_IOSS_VAT_TAB = "IOSS_VAT_TAB";
    private static final String SELECT_SQL = "select IOSS_VAT_ID from IOSS_VAT_TAB";
    private JdbcTemplate jdbcTemplate;
    private SynchronisationDao dao;

    @Autowired
    public SynchronisationDaoTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.dao = new SynchronisationDao(jdbcTemplate);
    }

    @Test
    @DisplayName("Ioss VAT record should be persisted successfully in PDS")
    void iossVatRecordCanBePersistedSuccessfully() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, TABLE_IOSS_VAT_TAB);
        IossVat iossVatRecord = iossVatCreateOf("IM0000000001");

        dao.create(iossVatRecord);

        int count = JdbcTestUtils.countRowsInTable(jdbcTemplate, TABLE_IOSS_VAT_TAB);
        assertThat(count, is(1));

        IossVat createdIossVatRecord = dao.retrieve(iossVatRecord.getIossVatId());
        assertAll("After update",
                () -> assertThat(createdIossVatRecord.getValidityEndDate(), equalTo(iossVatRecord.getValidityEndDate())),
                () -> assertThat(createdIossVatRecord.getValidityStartDate(), equalTo(iossVatRecord.getValidityStartDate())),
                () -> assertThat(createdIossVatRecord.getEuModificationDateTime(), equalTo(iossVatRecord.getEuModificationDateTime())),
                () -> assertThat(createdIossVatRecord.getIossVatId(), equalTo(iossVatRecord.getIossVatId())),
                () -> assertThat(createdIossVatRecord.getCreatedDateTime(), not(nullValue())),
                () -> assertThat(createdIossVatRecord.getUpdatedDateTime(), nullValue()),
                () -> assertThat(createdIossVatRecord.getId(), not(nullValue())));

    }

    @Test
    @DisplayName("Records with no optional data should be saved successfully")
    void iossVatRecordsWithoutOptionalDataCanBeSuccessfullySaved() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, TABLE_IOSS_VAT_TAB);

        IossVat iossVatRecord = iossVatCreateOf("IM0000000001");
        iossVatRecord.setValidityStartDate(null);
        iossVatRecord.setValidityEndDate(null);
        iossVatRecord.setEuModificationDateTime(null);

        dao.create(iossVatRecord);

        IossVat createdIossVatRecord = dao.retrieve(iossVatRecord.getIossVatId());
        assertThat(createdIossVatRecord, not(nullValue()));
        assertThat(createdIossVatRecord.getValidityStartDate(), not(nullValue()));
    }

    @Test
    @DisplayName("Existing IOSS VAT record should be updated successfully")
    @Sql("/sql/data.sql")
    void iossVatRecordUpdatedSuccessfully() {
        List<String> iossVatIds = jdbcTemplate.queryForList(SELECT_SQL, String.class);
        assertThat(iossVatIds, hasSize(greaterThan(0)));

        IossVat iossVatRecord = dao.retrieve(iossVatIds.get(0));
        iossVatRecord.setValidityEndDate(LocalDate.parse("2023-06-08"));
        LocalDateTime localDateTime = LocalDateTime.now();
        iossVatRecord.setEuModificationDateTime(localDateTime);

        dao.update(iossVatRecord);

        IossVat updatedIossVatRecord = dao.retrieve(iossVatRecord.getIossVatId());
        assertAll("After update",
                () -> assertThat(updatedIossVatRecord.getValidityEndDate(), equalTo(iossVatRecord.getValidityEndDate())),
                () -> assertThat(updatedIossVatRecord.getValidityStartDate(), equalTo(iossVatRecord.getValidityStartDate())),
                () -> assertThat(updatedIossVatRecord.getEuModificationDateTime(), equalTo(localDateTime)),
                () -> assertThat(updatedIossVatRecord.getIossVatId(), equalTo(iossVatRecord.getIossVatId())),
                () -> assertThat(updatedIossVatRecord.getCreatedDateTime(), equalTo(iossVatRecord.getCreatedDateTime())),
                () -> assertThat(updatedIossVatRecord.getUpdatedDateTime(), LocalDateTimeMatchers.sameDay(localDateTime)),
                () -> assertThat(updatedIossVatRecord.getId(), equalTo(iossVatRecord.getId())));
    }


    @Test
    @DisplayName("IOSS VAT record can be retrieved from PDS by ioss vat id")
    @Sql("/sql/data.sql")
    void iossVatRecordCanBeSuccessfullyRetrieved() {
        IossVat iossVatRecord = dao.retrieve("IM0000000101");
        assertThat(iossVatRecord.getIossVatId(), equalTo("IM0000000101"));
    }

    @Test
    @DisplayName("Existing IOSS VAT record should be deleted successfully")
    @Sql("/sql/data.sql")
    void iossVatRecordCanBeSuccessfullyDeletedIfExists() {
        List<String> iossVatIds = jdbcTemplate.queryForList(SELECT_SQL, String.class);
        assertThat(iossVatIds, hasSize(greaterThan(0)));
        IossVat iossVatRecord = dao.retrieve(iossVatIds.get(0));

        dao.delete(iossVatRecord.getIossVatId());

        IossVat deletedIossVatRecord = dao.retrieve(iossVatRecord.getIossVatId());
        assertThat(deletedIossVatRecord, is(nullValue()));
    }

    @Test
    @DisplayName("Delete operation for non-existent IOSS VAT record in PDS can be ignored without error")
    void iossVatRecordDeleteOperationCanBeIgnoredWithoutErrorWhenNotExist() {
        List<String> iossVatIds = jdbcTemplate.queryForList(SELECT_SQL, String.class);
        assertThat(iossVatIds, hasSize(equalTo(0)));

        IossVat iossVatRecord = iossVatDeleteOf("IM0000000001");

        dao.delete(iossVatRecord.getIossVatId());

        IossVat deletedIossVatRecord = dao.retrieve(iossVatRecord.getIossVatId());
        assertThat(deletedIossVatRecord, is(nullValue()));
    }
}
