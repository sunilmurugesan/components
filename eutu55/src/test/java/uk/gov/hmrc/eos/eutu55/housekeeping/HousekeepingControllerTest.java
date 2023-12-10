package uk.gov.hmrc.eos.eutu55.housekeeping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmrc.eos.eutu55.logger.LoggerComponent;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HousekeepingController.class)
class HousekeepingControllerTest {

    private static final String HOUSEKEEPING_URL = "/pds/cnit/eutu55/housekeeping/v1";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HousekeepingService service;

    @MockBean
    private LoggerComponent logger;

    @Test
    @DisplayName("Housekeeping post endpoint cal be invoked successfully with the request body")
    void housekeepingPostCanBePerformedWithRequestBody() throws Exception {
        mockMvc.perform(post(HOUSEKEEPING_URL)
                        .content("{\"retentionPeriod\": 90}")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Housekeeping post endpoint cal be invoked successfully without the request body")
    void housekeepingPostCanBePerformedWithoutRequestBody() throws Exception {
        mockMvc.perform(post(HOUSEKEEPING_URL)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isOk());
    }
}
