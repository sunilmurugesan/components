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
import uk.gov.hmrc.eos.eutu55.model.PingStatusResponse;
import uk.gov.hmrc.eos.eutu55.service.PingService;
import uk.gov.hmrc.eos.eutu55.utils.Status;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PingController.class)
 class PingControllerTest {


    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PingService pingService;

    @MockBean
    private LoggerComponent logger;

    private static final String USER_ID = "TEST_USER";


    @Test
    @DisplayName("/ping/v1 endpoint should be invoked and ping service is called")
    void testRetrieveEUSubscriptionStatus() throws Exception {
        PingStatusResponse response = new PingStatusResponse(Status.UP);

        when(pingService.ping(any())).thenReturn(response);

        mockMvc.perform(get("/pds/cnit/eutu55/ping/v1")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("userId", USER_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("UP"));

    }
}
