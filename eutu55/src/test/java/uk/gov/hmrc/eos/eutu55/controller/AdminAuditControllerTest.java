package uk.gov.hmrc.eos.eutu55.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmrc.eos.eutu55.logger.LoggerComponent;
import uk.gov.hmrc.eos.eutu55.model.AdminAuditDTO;
import uk.gov.hmrc.eos.eutu55.model.AdminAuditRequest;
import uk.gov.hmrc.eos.eutu55.model.AdminAuditResponse;
import uk.gov.hmrc.eos.eutu55.service.AdminAuditService;
import uk.gov.hmrc.eos.eutu55.utils.AdminActionType;
import uk.gov.hmrc.eos.eutu55.utils.Outcome;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminAuditController.class)
class AdminAuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminAuditService adminAuditService;

    @MockBean
    private LoggerComponent logger;

    @Test
    @DisplayName("AdminAuditAction endpoint should be invoked and adminAuditService is called and responds with multiple actions outcomes")
    void auditAdminData_shouldCallAdminAuditActionEndpointAndCallService() throws Exception {

        List<AdminActionType> actions = new ArrayList<>();
        actions.add(AdminActionType.RETRIEVAL);
        actions.add(AdminActionType.NOTIFICATION);
        List<Outcome> outcomes = new ArrayList<>();
        outcomes.add(Outcome.ACCEPTED);
        outcomes.add(Outcome.REJECTED);
        AdminAuditRequest reqMsg = AdminAuditRequest.builder()
                .date(LocalDate.now())
                .actions(actions)
                .outcomes(outcomes)
                .pageNumber(1)
                .build();

        List<AdminAuditDTO> adminActions = new ArrayList<>();
        AdminAuditDTO adminAuditDTO = AdminAuditDTO.builder()
                .action(AdminActionType.RETRIEVAL)
                .date(LocalDateTime.now())
                .outcome(Outcome.ACCEPTED)
                .build();
        adminActions.add(adminAuditDTO);
        adminAuditDTO = AdminAuditDTO.builder()
                .action(AdminActionType.NOTIFICATION)
                .date(LocalDateTime.now())
                .outcome(Outcome.REJECTED)
                .build();
        adminActions.add(adminAuditDTO);
        AdminAuditResponse respMsg = new AdminAuditResponse(2, adminActions);

        when(adminAuditService.getAdminAudits(any(AdminAuditRequest.class))).thenReturn(respMsg);
        mockMvc.perform(post("/pds/cnit/eutu55/admin/audit/v1")
                .content(objectMapper.writeValueAsString(reqMsg))
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andDo(print())
                .andExpect(jsonPath("$.adminActions[0].action").value("retrieval"))
                .andExpect(jsonPath("$.adminActions[0].outcome").value("ACCEPTED"))
                .andExpect(jsonPath("$.adminActions[1].action").value("notification"))
                .andExpect(jsonPath("$.adminActions[1].outcome").value("REJECTED"))
                .andExpect(jsonPath("$.recordCount").value(2))
                .andExpect(status().isOk());

        verify(adminAuditService).getAdminAudits(any(AdminAuditRequest.class));
    }
}
