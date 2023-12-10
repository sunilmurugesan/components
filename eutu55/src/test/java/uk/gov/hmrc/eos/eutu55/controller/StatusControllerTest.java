package uk.gov.hmrc.eos.eutu55.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import uk.gov.hmrc.eos.eutu55.logger.LoggerComponent;
import uk.gov.hmrc.eos.eutu55.model.StatusResultResponse;
import uk.gov.hmrc.eos.eutu55.service.StatusService;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatusController.class)
class StatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatusService statusService;

    @MockBean
    private LoggerComponent logger;

    private static final String USER_ID = "TEST_USER";


    @Test
    @DisplayName("Status enquiry endpoint should be invoked and status service is called")
    void testStatusEnquiry() throws Exception {

        StatusResultResponse statusResponse = StatusResultResponse.builder()
                .status(1)
                .totalItems(100L)
                .lastDisseminationDateTime(LocalDateTime.of(2022, 6, 9, 0, 0, 0)).build();
        when(statusService.enquire(any())).thenReturn(statusResponse);

        mockMvc.perform(get("/pds/cnit/eutu55/status/v1")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("userId", USER_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalItems").value(100));

    }
}
