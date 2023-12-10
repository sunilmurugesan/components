package uk.gov.hmrc.eos.eutu55.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmrc.eos.eutu55.entity.AdminAudit;
import uk.gov.hmrc.eos.eutu55.model.AdminAuditRequest;
import uk.gov.hmrc.eos.eutu55.repository.AdminAuditRepository;
import uk.gov.hmrc.eos.eutu55.utils.AdminActionType;
import uk.gov.hmrc.eos.eutu55.utils.Outcome;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmrc.eos.eutu55.utils.AdminActionType.RETRIEVAL;
import static uk.gov.hmrc.eos.eutu55.utils.AdminActionType.SUBSCRIPTION;
import static uk.gov.hmrc.eos.eutu55.utils.Outcome.ACCEPTED;
import static uk.gov.hmrc.eos.eutu55.utils.Outcome.REJECTED;

@SpringBootTest
@AutoConfigureMockMvc
class AdminAuditIT {

    private static final String POST_URL = "/pds/cnit/eutu55/admin/audit/v1";
    private static final int TWO = 2;
    private static final int PAGE_TWO = TWO;
    private final LocalDateTime FIXED_DATE_TIME = LocalDateTime.of(2022, Month.MARCH, 10, 10, 10, 10);
    private final LocalDateTime CURRENT_DATE_TIME = LocalDateTime.now();
    private static final String USER_ID = "TEST_USER";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminAuditRepository adminAuditRepository;

    @Autowired
    private ObjectMapper mapper;

    @Test
    @DisplayName("No data should be returned from empty table")
    void getAdminAuditsShouldNotReturnAnyDataOnEmptyTable() throws Exception {
        adminAuditRepository.deleteAll();

        mockMvc.perform(post(POST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(AdminAuditRequest.builder().build())))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.recordCount", equalTo(0)))
                .andExpect(jsonPath("$.adminActions", hasSize(0)));

    }

    @Test
    @DisplayName("One record should be returned when filter admin audit records by current date")
    void getAdminAuditsShouldReturnDataByDate() throws Exception {
        adminAuditRepository.save(buildAdminAudit(RETRIEVAL, ACCEPTED, FIXED_DATE_TIME));
        adminAuditRepository.save(buildAdminAudit(SUBSCRIPTION, REJECTED, FIXED_DATE_TIME));
        adminAuditRepository.save(buildAdminAudit(RETRIEVAL, ACCEPTED, CURRENT_DATE_TIME));

        mockMvc.perform(post(POST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(AdminAuditRequest.builder().date(CURRENT_DATE_TIME.toLocalDate()).build())))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.recordCount", equalTo(1)))
                .andExpect(jsonPath("$.adminActions", hasSize(1)))
                .andExpect(jsonPath("$.adminActions[0].action", is(RETRIEVAL.name().toLowerCase())))
                .andExpect(jsonPath("$.adminActions[0].date", is(CURRENT_DATE_TIME.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))))
                .andExpect(jsonPath("$.adminActions[0].outcome", is(ACCEPTED.name())));
    }

    @Test
    @DisplayName("Two records should be returned in descending order by date when filter admin audit records by action")
    void getAdminAuditsShouldReturnDataByAction() throws Exception {
        adminAuditRepository.save(buildAdminAudit(RETRIEVAL, REJECTED, FIXED_DATE_TIME));
        adminAuditRepository.save(buildAdminAudit(RETRIEVAL, ACCEPTED, CURRENT_DATE_TIME));

        mockMvc.perform(post(POST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(AdminAuditRequest.builder().actions(List.of(RETRIEVAL)).build())))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.recordCount", equalTo(TWO)))
                .andExpect(jsonPath("$.adminActions", hasSize(TWO)))
                .andExpect(jsonPath("$.adminActions[0].action", is(RETRIEVAL.name().toLowerCase())))
                .andExpect(jsonPath("$.adminActions[0].date", is(CURRENT_DATE_TIME.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))))
                .andExpect(jsonPath("$.adminActions[0].outcome", is(ACCEPTED.name())))
                .andExpect(jsonPath("$.adminActions[1].action", is(RETRIEVAL.name().toLowerCase())))
                .andExpect(jsonPath("$.adminActions[1].date", is(FIXED_DATE_TIME.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))))
                .andExpect(jsonPath("$.adminActions[1].outcome", is(REJECTED.name())));

    }

    @Test
    @DisplayName("One record should be returned when filter admin audit records by outcome")
    void getAdminAuditsShouldReturnDataByOutcome() throws Exception {
        adminAuditRepository.save(buildAdminAudit(RETRIEVAL, REJECTED, FIXED_DATE_TIME));
        adminAuditRepository.save(buildAdminAudit(RETRIEVAL, ACCEPTED, CURRENT_DATE_TIME));

        mockMvc.perform(post(POST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(AdminAuditRequest.builder().outcomes(List.of(ACCEPTED)).build())))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.recordCount", equalTo(1)))
                .andExpect(jsonPath("$.adminActions", hasSize(1)))
                .andExpect(jsonPath("$.adminActions[0].action", is(RETRIEVAL.name().toLowerCase())))
                .andExpect(jsonPath("$.adminActions[0].date", is(CURRENT_DATE_TIME.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))))
                .andExpect(jsonPath("$.adminActions[0].outcome", is(ACCEPTED.name())));

    }

    @Test
    @DisplayName("No records returned for page two if total number of records are less than page size")
    void getAdminAuditsShouldNotReturnDataByOffset() throws Exception {
        adminAuditRepository.save(buildAdminAudit(RETRIEVAL, REJECTED, FIXED_DATE_TIME));
        adminAuditRepository.save(buildAdminAudit(RETRIEVAL, ACCEPTED, CURRENT_DATE_TIME));

        mockMvc.perform(post(POST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(AdminAuditRequest.builder().pageNumber(PAGE_TWO).build())))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.recordCount", equalTo(TWO)))
                .andExpect(jsonPath("$.adminActions", hasSize(0)));
    }

    private AdminAudit buildAdminAudit(AdminActionType actionType, Outcome outcome, LocalDateTime requestTime) {
        AdminAudit adminAudit = AdminAudit.buildAdminAudit(actionType, outcome, USER_ID);
        adminAudit.setRequestTime(requestTime);
        return adminAudit;
    }

    @AfterEach
    void tearDown() {
        adminAuditRepository.deleteAll();
    }
}
