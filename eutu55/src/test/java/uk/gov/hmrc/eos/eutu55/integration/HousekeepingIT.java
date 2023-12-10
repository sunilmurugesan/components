package uk.gov.hmrc.eos.eutu55.integration;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmrc.eos.eutu55.housekeeping.HousekeepingService;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"housekeeping.frequency=*/2 * * * * *",
        "housekeeping.retention-period=7"})
public class HousekeepingIT {

    private static final String HOUSEKEEPING_URL = "/pds/cnit/eutu55/housekeeping/v1";
    private static final String SELECT_ADMIN_AUDIT_SQL = "select ACTION from ADMIN_AUDIT_TAB";
    private static final String SELECT_IOSS_VAT_AUDIT_SQL = "select IOSS_VAT_ID from IOSS_VAT_AUDIT_TAB";

    private static final String USER_ID = "7800000";

    private final MockMvc mockMvc;
    private final JdbcTemplate jdbcTemplate;

    @SpyBean
    HousekeepingService housekeepingService;

    @Autowired
    public HousekeepingIT(MockMvc mockMvc, JdbcTemplate jdbcTemplate) {
        this.mockMvc = mockMvc;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    public void setup() {
        Clock clock = Clock.fixed(Instant.parse("2023-02-07T10:09:59.00Z"), ZoneId.systemDefault());
        ReflectionTestUtils.setField(housekeepingService, "clock", clock);
    }

    @Test
    @DisplayName("Housekeeping can be performed in the configured intervals")
    void housekeepingCanBePerformedInTheScheduledIntervals() {
        createAdminAuditRecords();
        createIossVatAuditRecords();

        await().atMost(Duration.of(5, ChronoUnit.SECONDS)).untilAsserted(() -> {
            verify(housekeepingService, atLeast(2)).performHousekeeping();

            List<String> actions = jdbcTemplate.queryForList(SELECT_ADMIN_AUDIT_SQL, String.class);
            List<String> iossVatIds = jdbcTemplate.queryForList(SELECT_IOSS_VAT_AUDIT_SQL, String.class);
            assertAll("Remaining records after Housekeeping",
                    () -> assertThat(actions, Matchers.<Collection<String>>allOf(
                            hasSize(is(3)),
                            hasItems("STATUS", "SUBSCRIPTION", "NOTIFICATION"))),
                    () -> assertThat(iossVatIds, Matchers.<Collection<String>>allOf(
                            hasSize(is(3)),
                            hasItems("IM0000000002", "IM0000000004", "IM0000000005"))));
        });
    }

    @Test
    @DisplayName("Housekeeping can be performed ad-hoc by called the post endpoint")
    void housekeepingCanBePerformedAdhocByInvokingEndpoint() throws Exception {
        createAdminAuditRecords();
        createIossVatAuditRecords();

        mockMvc.perform(post(HOUSEKEEPING_URL)
                        .content("{\"retentionPeriod\": 30}")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());

        List<String> actions = jdbcTemplate.queryForList(SELECT_ADMIN_AUDIT_SQL, String.class);
        assertAll("Remaining records in ADMIN_AUDIT_TAB after Housekeeping",
                () -> assertThat(actions, Matchers.<Collection<String>>allOf(
                        hasSize(is(4)),
                        hasItems("STATUS", "PING", "SUBSCRIPTION", "NOTIFICATION"))));

        List<String> iossVatIds = jdbcTemplate.queryForList(SELECT_IOSS_VAT_AUDIT_SQL, String.class);
        assertAll("Remaining records in ADMIN_AUDIT_TAB after Housekeeping",
                () -> assertThat(iossVatIds, Matchers.<Collection<String>>allOf(
                        hasSize(is(3)),
                        hasItems("IM0000000002", "IM0000000004", "IM0000000005"))));
    }

    private void createAdminAuditRecords() {
        String insertSQL = "insert into ADMIN_AUDIT_TAB (ID, ACTION, RESPONSE_OUTCOME, REQUEST_TIME, USER_ID) " +
                "values(ADMIN_AUDIT_TAB_SEQ.nextval, ?, 1, ?, " + USER_ID + ")";
        jdbcTemplate.update(insertSQL, "RETRIEVAL", toTimestamp("2022-01-06 11:00:00.0"));
        jdbcTemplate.update(insertSQL, "STATUS", toTimestamp("2023-01-31 10:09:59.0"));
        jdbcTemplate.update(insertSQL, "PING", toTimestamp("2023-01-30 00:00:00.00"));
        jdbcTemplate.update(insertSQL, "SUBSCRIPTION", toTimestamp("2023-02-07 10:09:59.00"));
        jdbcTemplate.update(insertSQL, "NOTIFICATION", toTimestamp("2023-03-08 09:09:09.00"));
    }

    private void createIossVatAuditRecords() {
        String insertSQL = "insert into IOSS_VAT_AUDIT_TAB (IOSS_VAT_ID, OPERATION, OUTCOME, AUDIT_DATE) values (?, ?, 1, ?)";
        jdbcTemplate.update(insertSQL, "IM0000000001", "C", toTimestamp("2020-12-01 11:00:00.0"));
        jdbcTemplate.update(insertSQL, "IM0000000002", "C", toTimestamp("2023-01-31 10:09:59.0"));
        jdbcTemplate.update(insertSQL, "IM0000000003", "U", toTimestamp("2022-02-01 00:00:00.00"));
        jdbcTemplate.update(insertSQL, "IM0000000004", "U", toTimestamp("2023-02-07 10:09:59.00"));
        jdbcTemplate.update(insertSQL, "IM0000000005", "D", toTimestamp("2023-02-08 09:09:09.00"));
    }

    private Timestamp toTimestamp(String dateTimeText) {
        return Timestamp.valueOf(dateTimeText);
    }

    @AfterEach
    void tearDown() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "ADMIN_AUDIT_TAB");
        JdbcTestUtils.deleteFromTables(jdbcTemplate, "IOSS_VAT_AUDIT_TAB");
    }
}
