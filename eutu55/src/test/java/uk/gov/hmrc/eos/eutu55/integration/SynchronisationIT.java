package uk.gov.hmrc.eos.eutu55.integration;

import org.exparity.hamcrest.date.LocalDateMatchers;
import org.exparity.hamcrest.date.LocalDateTimeMatchers;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmrc.eos.eutu55.dao.SynchronisationDao;
import uk.gov.hmrc.eos.eutu55.helper.TestHelper;
import uk.gov.hmrc.eos.eutu55.model.IossVat;
import uk.gov.hmrc.eos.eutu55.utils.Outcome;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;
import static uk.gov.hmrc.eos.eutu55.config.RequestCorrelationId.X_CORRELATION_ID;

@SpringBootTest
@AutoConfigureMockMvc
public class SynchronisationIT {

    private static final String TABLE_IOSS_VAT_TAB = "IOSS_VAT_TAB";
    private static final String TABLE_IOSS_VAT_AUDIT_TAB = "IOSS_VAT_AUDIT_TAB";
    private static final String SELECT_SQL = "select IOSS_VAT_ID from IOSS_VAT_TAB";
    private static final String SELECT_AUDIT_SQL = "select IOSS_VAT_ID from IOSS_VAT_AUDIT_TAB";
    private static final String POST_URL = "/pds/cnit/eutu55/synchronisation/v1";
    private static final String SELECT_ALL_SQL = "SELECT * FROM IOSS_VAT_TAB where IOSS_VAT_ID = ?";


    private final MockMvc mockMvc;
    private final JdbcTemplate jdbcTemplate;
    @Autowired
    private SynchronisationDao dao;

    @Autowired
    public SynchronisationIT(final MockMvc mockMvc, final JdbcTemplate jdbcTemplate) {
        this.mockMvc = mockMvc;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Test
    @DisplayName("When a valid request payload is received with create operations then all ioss vat records are persisted successfully in PDS")
    void createIossVatData() throws Exception {
        mockMvc.perform(post(POST_URL)
                        .content(request("valid-create.xml"))
                        .contentType(MediaType.APPLICATION_XML)
                        .header(X_CORRELATION_ID, "f7b74594-b6a7-45e6-a69c-b2563381aed9"))
                .andExpect(status().isOk())
                .andExpect(header().string(X_CORRELATION_ID, "f7b74594-b6a7-45e6-a69c-b2563381aed9"))
                .andExpect(content().xml(response("success.xml")));

        List<String> iossVatIds = jdbcTemplate.queryForList(SELECT_SQL, String.class);
        assertThat(iossVatIds, Matchers.<Collection<String>>allOf(
                hasSize(is(5)),
                hasItems("IM0000000001", "IM0000000002", "IM0000000003", "IM0000000004", "IM0000000005")));
    }

    @Test
    @DisplayName("When a valid request payload is received with update operations then the matching ioss vat records are updated successfully in PDS")
    @Sql("/sql/data.sql")
    void updateIossVatData() throws Exception {
        String iossVatIdUnderTest = "IM0000000101";
        IossVat iossVatBefore = findByIossVatId(iossVatIdUnderTest);
        assertAll("Before update",
                () -> assertThat(iossVatBefore, is(not(nullValue()))),
                () -> assertThat(iossVatBefore.getValidityEndDate(), LocalDateMatchers.sameDay(LocalDate.parse("2022-01-05"))),
                () -> assertThat(iossVatBefore.getEuModificationDateTime(), LocalDateTimeMatchers.sameDay(LocalDateTime.parse("2022-01-06T11:43:17"))),
                () -> assertThat(iossVatBefore.getUpdatedDateTime(), is(nullValue())));

        mockMvc.perform(post(POST_URL)
                        .content(request("valid-update.xml"))
                        .contentType(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(content().xml(response("success.xml")));

        IossVat iossVatAfter = findByIossVatId(iossVatIdUnderTest);
        assertAll("After update",
                () -> assertThat(iossVatAfter.getValidityEndDate(), LocalDateMatchers.sameDay(LocalDate.parse("2022-01-13"))),
                () -> assertThat(iossVatAfter.getEuModificationDateTime(), LocalDateTimeMatchers.sameDay(LocalDateTime.parse("2022-01-07T14:36:46"))),
                () -> assertThat(iossVatAfter.getUpdatedDateTime(), is(not(nullValue()))));
    }

    @Test
    @DisplayName("When a valid request payload is received with update operations and the record does not exist in PDS, " +
            "then the ioss vat records are created successfully")
    void upsertIossVatData() throws Exception {
        int count = JdbcTestUtils.countRowsInTable(jdbcTemplate, TABLE_IOSS_VAT_TAB);
        assertAll("Before upsert",
                () -> assertThat(count, is(0)));

        mockMvc.perform(post(POST_URL)
                        .content(request("valid-update.xml"))
                        .contentType(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(content().xml(response("success.xml")));

        List<String> iossVatIds = jdbcTemplate.queryForList(SELECT_SQL, String.class);
        assertAll("After upsert",
                () -> assertThat(iossVatIds, Matchers.<Collection<String>>allOf(
                        hasSize(is(5)),
                        hasItems("IM0000000101", "IM0000000102", "IM0000000103", "IM0000000104", "IM0000000105"))));
    }


    @Test
    @DisplayName("When a valid request payload is received with delete operations, " +
            "Then the matching ioss vat records are deleted successfully from PDS")
    @Sql("/sql/data.sql")
    void deleteIossVatData() throws Exception {
        int count = JdbcTestUtils.countRowsInTable(jdbcTemplate, TABLE_IOSS_VAT_TAB);
        assertAll("Before delete",
                () -> assertThat(count, is(5)));
        mockMvc.perform(post(POST_URL)
                        .content(request("valid-delete.xml"))
                        .contentType(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(content().xml(response("success.xml")));

        List<String> iossVatIds = jdbcTemplate.queryForList(SELECT_SQL, String.class);
        assertAll("After delete",
                () -> assertThat(iossVatIds, Matchers.<Collection<String>>allOf(
                        hasSize(is(2)),
                        hasItems("IM0000000102", "IM0000000104"))));
    }

    @Test
    @DisplayName("When a valid request payload is received from EIS with the combination create, update and delete operations, " +
            "Then the records are processed in sequential order and successful outcome is returned")
    void processIossVatRecordsSequentially() throws Exception {
        mockMvc.perform(post(POST_URL)
                        .content(request("valid-create-update-delete.xml"))
                        .contentType(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_XML_VALUE))
                .andExpect(content().contentType("application/xml"))
                .andExpect(content().xml(response("success.xml")))
                .andExpect(xpath("PublishNumbersRespMsg/publicationResults/outcome").string(Outcome.ACCEPTED.getStringValue()));

        List<String> iossVatIds = jdbcTemplate.queryForList(SELECT_SQL, String.class);
        assertAll("After successful dissemination",
                () -> assertThat(iossVatIds, Matchers.<Collection<String>>allOf(
                        hasSize(is(3)),
                        hasItems("IM1000000001", "IM1000000002", "IM1000000004"))));
        assertThat(findByIossVatId("IM1000000001").getValidityEndDate(), LocalDateMatchers.sameDay(LocalDate.parse("2023-06-07")));
        assertThat(findByIossVatId("IM1000000004").getValidityEndDate(), LocalDateMatchers.sameDay(LocalDate.parse("2023-06-06")));
    }

    @Test
    @DisplayName("When an invalid message is received from EIS, then it is validated against the schema and correct fault is returned")
    void processInvalidRequest() throws Exception {
        mockMvc.perform(post(POST_URL)
                        .content(request("invalid-create-operation.xml"))
                        .contentType(MediaType.APPLICATION_XML))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(response("fault-invalid-request.xml")));
    }

    @Test
    @DisplayName("When an invalid empty message is received from EIS, then it is validated against the schema and correct fault is returned")
    void processInvalidEmptyRequest() throws Exception {
        mockMvc.perform(post(POST_URL)
                        .content(request("empty-request.xml"))
                        .contentType(MediaType.APPLICATION_XML))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(response("fault-invalid-request.xml")));
    }

    @Test
    @DisplayName("When a message is received from EIS where the number of IOSS records exceeding the limit, " +
            "Then it is validated against the schema " +
            "And correct fault is returned")
    void processInvalidRequestExceededLimit() throws Exception {
        mockMvc.perform(post(POST_URL)
                        .content(request("invalid-limit-exceeded.xml"))
                        .contentType(MediaType.APPLICATION_XML))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(response("fault-limit-exceeded.xml")));
    }

    @Test
    @DisplayName("When a valid request payload is received with the combination create, update and delete operations, " +
            "Then all the records are audited in the order that's been received")
    void iossVatRecordsAreAudited() throws Exception {
        int count = JdbcTestUtils.countRowsInTable(jdbcTemplate, TABLE_IOSS_VAT_AUDIT_TAB);
        assertAll("Before audit",
                () -> assertThat(count, is(0)));

        mockMvc.perform(post(POST_URL)
                        .content(request("valid-create-update-delete.xml"))
                        .contentType(MediaType.APPLICATION_XML))
                .andExpect(status().isOk())
                .andExpect(content().xml(response("success.xml")));

        List<String> iossVatIds = jdbcTemplate.queryForList(SELECT_AUDIT_SQL, String.class);
        assertAll("After audit",
                () -> assertThat(iossVatIds, Matchers.<Collection<String>>allOf(
                        hasSize(is(9)),
                        hasItems("IM1000000001", "IM1000000002", "IM1000000003", "IM1000000004"))));
        assertAll("Record count in audit table",
                () -> assertThat(countRowsInAuditTable("IM1000000001"), equalTo(2)),
                () -> assertThat(countRowsInAuditTable("IM1000000002"), equalTo(1)),
                () -> assertThat(countRowsInAuditTable("IM1000000003"), equalTo(3)),
                () -> assertThat(countRowsInAuditTable("IM1000000003"), equalTo(3)));
    }

    private IossVat findByIossVatId(String iossVatId) {
        return dao.retrieve(iossVatId);
    }

    private int countRowsInAuditTable(String iossVatId) {
        return JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, TABLE_IOSS_VAT_AUDIT_TAB,
                "IOSS_VAT_ID ='" + iossVatId + "'");
    }

    private String request(String filename) {
        return TestHelper.request("synchronisation/" + filename);
    }

    private String response(String filename) {
        return TestHelper.response("synchronisation/" + filename);
    }

    @AfterEach
    void tearDown() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, TABLE_IOSS_VAT_TAB);
        JdbcTestUtils.deleteFromTables(jdbcTemplate, TABLE_IOSS_VAT_AUDIT_TAB);
    }
}
