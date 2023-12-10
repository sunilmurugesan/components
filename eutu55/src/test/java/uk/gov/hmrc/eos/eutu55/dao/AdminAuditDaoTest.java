package uk.gov.hmrc.eos.eutu55.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmrc.eos.eutu55.entity.AdminAudit;
import uk.gov.hmrc.eos.eutu55.model.AdminAuditRequest;
import uk.gov.hmrc.eos.eutu55.repository.AdminAuditRepository;
import uk.gov.hmrc.eos.eutu55.utils.AdminActionType;
import uk.gov.hmrc.eos.eutu55.utils.Outcome;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static uk.gov.hmrc.eos.eutu55.utils.AdminActionType.NOTIFICATION;
import static uk.gov.hmrc.eos.eutu55.utils.AdminActionType.PING;
import static uk.gov.hmrc.eos.eutu55.utils.AdminActionType.RETRIEVAL;
import static uk.gov.hmrc.eos.eutu55.utils.AdminActionType.STATUS;
import static uk.gov.hmrc.eos.eutu55.utils.AdminActionType.SUBSCRIPTION;
import static uk.gov.hmrc.eos.eutu55.utils.Outcome.ACCEPTED;
import static uk.gov.hmrc.eos.eutu55.utils.Outcome.REJECTED;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@DataJpaTest(includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Repository.class))
class AdminAuditDaoTest {

    private final LocalDateTime FIXED_DATE_TIME = LocalDateTime.of(2022, Month.MARCH, 10, 10, 10, 10);
    private final LocalDateTime CURRENT_DATE_TIME = LocalDateTime.now();
    private static final String TABLE_ADMIN_AUDIT_TAB = "ADMIN_AUDIT_TAB";
    private static final String USER_ID = "TEST_USER";

    private final JdbcTemplate jdbcTemplate;
    private final AdminAuditDao dao;
    @Autowired
    private AdminAuditRepository repository;

    @Autowired
    public AdminAuditDaoTest(final JdbcTemplate jdbcTemplate, final AdminAuditDao dao) {
        this.jdbcTemplate = jdbcTemplate;
        this.dao = dao;
    }

    @BeforeEach
    void setup() {
        dao.save(buildAdminAudit(RETRIEVAL, ACCEPTED, FIXED_DATE_TIME));
        dao.save(buildAdminAudit(SUBSCRIPTION, REJECTED, FIXED_DATE_TIME));
        dao.save(buildAdminAudit(RETRIEVAL, ACCEPTED, CURRENT_DATE_TIME));
    }

    @Test
    @DisplayName("When no filters are provided, then all admin audit records can be retrieved")
    void allAdminAuditRecordsCanBeRetrievedWhenNoFilters() {
        Page<AdminAudit> pagedRecords = dao.filterBy(AdminAuditRequest.builder().build());
        assertThat(pagedRecords.getContent(), hasSize(is(3)));
        List<Outcome> collect = pagedRecords.getContent().stream().map(AdminAudit::getResponseOutcome).collect(Collectors.toList());
        assertThat(collect, hasItems(ACCEPTED, REJECTED));
        List<AdminActionType> actions = pagedRecords.getContent().stream().map(AdminAudit::getAction).collect(Collectors.toList());
        assertThat(actions, hasItems(RETRIEVAL, SUBSCRIPTION));
    }

    @Test
    @DisplayName("All retrieved admin audit records can be paginated and ordered by date")
    void adminAuditRecordsCanBePaginatedAndOrderedByDate() {
        repository.deleteAll();

        createAdminAuditRecords(25);
        int maxRecordsPerPage = 10;

        int page = 1;
        Page<AdminAudit> pagedRecords = dao.filterBy(AdminAuditRequest.builder().pageNumber(page).build());
        assertThat(pagedRecords.getTotalElements(), equalTo(25L));
        assertThat(pagedRecords.getContent(), hasSize(is(maxRecordsPerPage)));
        assertThat(pagedRecords.getContent().stream().findFirst().get().getRequestTime(), equalTo(FIXED_DATE_TIME.withDayOfMonth(25)));

        page = 2;
        pagedRecords = dao.filterBy(AdminAuditRequest.builder().pageNumber(page).build());
        assertThat(pagedRecords.getContent(), hasSize(is(maxRecordsPerPage)));
        assertThat(pagedRecords.getContent().stream().findFirst().get().getRequestTime(), equalTo(FIXED_DATE_TIME.withDayOfMonth(15)));

        page = 3;
        pagedRecords = dao.filterBy(AdminAuditRequest.builder().pageNumber(page).build());
        assertThat(pagedRecords.getContent(), hasSize(is(5)));
        assertThat(pagedRecords.getContent().stream().findFirst().get().getRequestTime(), equalTo(FIXED_DATE_TIME.withDayOfMonth(5)));
        assertThat(pagedRecords.getContent().stream().skip(pagedRecords.getContent().size() - 1).findFirst().get().getRequestTime(),
                equalTo(FIXED_DATE_TIME.withDayOfMonth(1)));
    }

    @Test
    @DisplayName("When date filter is supplied, then admin audit records can be filtered by date")
    void adminAuditRecordsCanBeFilteredByDate() {
        Page<AdminAudit> pagedRecords = dao.filterBy(AdminAuditRequest.builder().date(FIXED_DATE_TIME.toLocalDate()).build());
        assertThat(pagedRecords.getContent(), hasSize(is(2)));
        assertThat(pagedRecords.getContent().stream().findAny().get().getRequestTime(), equalTo(FIXED_DATE_TIME));
    }

    @Test
    @DisplayName("When action filter is supplied, then admin audit records can be filtered by action")
    void adminAuditRecordsCanBeFilteredByActionRetrieval() {
        Page<AdminAudit> pagedRecords = dao.filterBy(AdminAuditRequest.builder().actions(List.of(RETRIEVAL)).build());
        assertThat(pagedRecords.getContent(), hasSize(is(2)));
        assertThat(pagedRecords.getContent().stream().findAny().get().getAction(), equalTo(RETRIEVAL));
    }

    @Test
    @DisplayName("When actions' filter is supplied, then admin audit records can be filtered by multiple actions")
    void adminAuditRecordsCanBeFilteredByActions() {
        dao.save(buildAdminAudit(NOTIFICATION, ACCEPTED, CURRENT_DATE_TIME));
        Page<AdminAudit> pagedRecords = dao.filterBy(AdminAuditRequest.builder().actions(List.of(RETRIEVAL, SUBSCRIPTION)).build());
        List<AdminActionType> actions = pagedRecords.getContent().stream().map(AdminAudit::getAction).collect(Collectors.toList());
        assertThat(actions, hasItems(RETRIEVAL, SUBSCRIPTION));
        assertThat(actions, not(NOTIFICATION));
    }

    @Test
    @DisplayName("Outcome ACCEPTED records should be returned")
    void adminAuditRecordsCanBeFilteredByOutcomeAccepted() {
        Page<AdminAudit> records = dao.filterBy(AdminAuditRequest.builder().outcomes(List.of(ACCEPTED)).build());
        assertThat(records.getContent(), hasSize(is(2)));
        assertThat(records.getContent().stream().findAny().get().getResponseOutcome(), equalTo(ACCEPTED));
    }

    @Test
    @DisplayName("All records should be returned for all possible outcomes")
    void adminAuditRecordsCanBeFilteredByOutcomes() {
        Page<AdminAudit> records = dao.filterBy(AdminAuditRequest.builder().outcomes(List.of(ACCEPTED, REJECTED)).build());
        assertThat(records.getContent(), hasSize(is(3)));
        List<Outcome> collect = records.getContent().stream().map(AdminAudit::getResponseOutcome).collect(Collectors.toList());
        assertThat(collect, hasItems(ACCEPTED, REJECTED));
    }

    @Test
    @DisplayName("All records should be returned for actions and outcomes")
    void adminAuditRecordsCanBeFilteredByActionsAndOutcomes() {
        Page<AdminAudit> records = dao.filterBy(AdminAuditRequest.builder()
                .outcomes(List.of(ACCEPTED, REJECTED))
                .actions(List.of(RETRIEVAL, SUBSCRIPTION)).build());
        List<Outcome> collect = records.getContent().stream().map(AdminAudit::getResponseOutcome).collect(Collectors.toList());
        assertThat(collect, hasItems(ACCEPTED, REJECTED));
        List<AdminActionType> actions = records.getContent().stream().map(AdminAudit::getAction).collect(Collectors.toList());
        assertThat(actions, hasItems(RETRIEVAL, SUBSCRIPTION));
    }


    @Test
    @DisplayName("One record should be returned for current date")
    void adminAuditRecordsCanBeFilteredByDateActionsAndOutcomesPage1() {
        Page<AdminAudit> records = dao.filterBy(AdminAuditRequest.builder()
                .date(CURRENT_DATE_TIME.toLocalDate())
                .pageNumber(1)
                .outcomes(List.of(ACCEPTED, REJECTED))
                .actions(List.of(RETRIEVAL, SUBSCRIPTION)).build());
        List<Outcome> collect = records.getContent().stream().map(AdminAudit::getResponseOutcome).collect(Collectors.toList());
        assertThat(collect, hasItems(ACCEPTED));
        List<AdminActionType> actions = records.getContent().stream().map(AdminAudit::getAction).collect(Collectors.toList());
        assertThat(actions, hasItems(RETRIEVAL));
    }

    @Test
    @DisplayName("Total available records 3 and no records should be returned for page 2")
    void adminAuditRecordsCanBeFilteredByDateActionsAndOutcomesPage2() {
        Page<AdminAudit> records = dao.filterBy(AdminAuditRequest.builder()
                .date(CURRENT_DATE_TIME.toLocalDate())
                .pageNumber(2)
                .outcomes(List.of(ACCEPTED, REJECTED))
                .actions(List.of(RETRIEVAL, SUBSCRIPTION)).build());
        List<Outcome> collect = records.getContent().stream().map(AdminAudit::getResponseOutcome).collect(Collectors.toList());
        assertThat(collect, hasSize(0));
    }

    @Test
    @DisplayName("When Request is built with pageNumber 0, total available pages should be only 1")
    void adminAuditRecordsFilteredByRequestWithPageNumberSetToZero() {
        Page<AdminAudit> pagedRecords = dao.filterBy(AdminAuditRequest.builder().pageNumber(0).build());
        assertThat(pagedRecords.getTotalPages(), is(1));
    }

    @Test
    @DisplayName("When Request is built with pageNumber null, total available pages should be only 1")
    void adminAuditRecordsFilteredByRequestWithPageNumberSetToNull() {
        Page<AdminAudit> pagedRecords = dao.filterBy(AdminAuditRequest.builder().pageNumber(null).build());
        assertThat(pagedRecords.getTotalPages(), is(1));
    }

    @Test
    @DisplayName("When Request is built with actions null, total available pages should be only 1")
    void adminAuditRecordsFilteredByRequestWithActionsSetToNull() {
        Page<AdminAudit> pagedRecords = dao.filterBy(AdminAuditRequest.builder().actions(null).build());
        assertThat(pagedRecords.getTotalPages(), is(1));
    }

    @Test
    @DisplayName("When Request is built with actions empty, total available pages should be only 1")
    void adminAuditRecordsFilteredByRequestWithActionsSetToEmptyList() {
        Page<AdminAudit> pagedRecords = dao.filterBy(AdminAuditRequest.builder().actions(Collections.emptyList()).build());
        assertThat(pagedRecords.getTotalPages(), is(1));
    }

    @Test
    @DisplayName("When Request is built with outcomes null, total available pages should be only 1")
    void adminAuditRecordsFilteredByRequestWithOutcomesSetToNull() {
        Page<AdminAudit> pagedRecords = dao.filterBy(AdminAuditRequest.builder().outcomes(null).build());
        assertThat(pagedRecords.getTotalPages(), is(1));
    }

    @Test
    @DisplayName("When Request is built with outcomes empty, total available pages should be only 1")
    void adminAuditRecordsFilteredByRequestWithOutcomesSetToEmptyList() {
        Page<AdminAudit> pagedRecords = dao.filterBy(AdminAuditRequest.builder().outcomes(Collections.emptyList()).build());
        assertThat(pagedRecords.getTotalPages(), is(1));
    }

    @Test
    @DisplayName("Admin audit records can be deleted based on supplied date")
    void adminAuditRecordsCanBeDeletedByRequestTime() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, TABLE_ADMIN_AUDIT_TAB);

        dao.save(buildAdminAudit(RETRIEVAL, ACCEPTED, toLocalDateTime("2020-12-01 11:00:00.0")));
        dao.save(buildAdminAudit(STATUS, REJECTED, toLocalDateTime("2023-01-31 10:09:59.0")));
        dao.save(buildAdminAudit(PING, ACCEPTED, toLocalDateTime("2022-02-01 00:00:00.00")));
        dao.save(buildAdminAudit(SUBSCRIPTION, ACCEPTED, toLocalDateTime("2023-02-06 10:09:59.00")));
        dao.save(buildAdminAudit(NOTIFICATION, ACCEPTED, toLocalDateTime("2023-03-08 09:09:09.00")));

        LocalDateTime dateForDeletion = Timestamp.valueOf("2023-02-06 00:00:00.0").toLocalDateTime();
        dao.deleteByRequestTime(dateForDeletion);

        Page<AdminAudit> pagedRecords = dao.filterBy(AdminAuditRequest.builder().build());
        assertThat(pagedRecords.getContent(), hasSize(is(2)));
        List<AdminActionType> actions = pagedRecords.getContent().stream().map(AdminAudit::getAction).collect(Collectors.toList());
        assertThat(actions, hasItems(SUBSCRIPTION, NOTIFICATION));

    }

    private LocalDateTime toLocalDateTime(String dateTimeText) {
        return Timestamp.valueOf(dateTimeText).toLocalDateTime();
    }

    private void createAdminAuditRecords(int count) {
        new Random().ints(1, count + 1).distinct().limit(count)
                .mapToObj(this::buildAdminAudit).forEach(dao::save);
    }

    private AdminAudit buildAdminAudit(int date) {
        LocalDateTime requestTime = FIXED_DATE_TIME.withDayOfMonth(date);
        return buildAdminAudit(RETRIEVAL, ACCEPTED, requestTime);
    }

    private AdminAudit buildAdminAudit(AdminActionType actionType, Outcome outcome, LocalDateTime requestTime) {
        AdminAudit adminAudit = AdminAudit.buildAdminAudit(actionType, outcome, USER_ID);
        adminAudit.setRequestTime(requestTime);
        return adminAudit;
    }

    @AfterEach
    void tearDown() {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, TABLE_ADMIN_AUDIT_TAB);
    }

}
