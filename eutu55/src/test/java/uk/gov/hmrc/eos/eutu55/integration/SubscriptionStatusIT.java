package uk.gov.hmrc.eos.eutu55.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmrc.eos.eutu55.entity.SubscriptionStatus;
import uk.gov.hmrc.eos.eutu55.repository.SubscriptionStatusRepository;
import uk.gov.hmrc.eos.eutu55.utils.SubscriptionStatusType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmrc.eos.eutu55.service.SubscriptionStatusService.SUBSCRIPTION_STATUS_ID;

@SpringBootTest
@AutoConfigureMockMvc
public class SubscriptionStatusIT {

    private final MockMvc mockMvc;
    private final SubscriptionStatusRepository subscriptionStatusRepository;
    private static final String USER_ID = "TEST_USER";

    @Autowired
    public SubscriptionStatusIT(final MockMvc mockMvc,
                                final SubscriptionStatusRepository subscriptionStatusRepository){
        this.mockMvc = mockMvc;
        this.subscriptionStatusRepository = subscriptionStatusRepository;
    }

    @Test
    @DisplayName("Empty record should be returned when subscription status record does not exist in PDS")
    void shouldReturnEmptyRecordWhenSubscriptionStatusDoesNotExist() throws Exception {
        subscriptionStatusRepository.deleteAll();
        mockMvc.perform(get("/pds/cnit/eutu55/subscription/status/v1")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("userId", USER_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("subscription/status endpoint should be invoked and retrieval service is called")
    void testRetrieveSubscriptionStatus() throws Exception {
        SubscriptionStatus subscriptionStatus = SubscriptionStatus.builder()
                .id(1)
                .status(SubscriptionStatusType.ON)
                .requestTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(1652725557), ZoneId.systemDefault()))
                .build();
        subscriptionStatusRepository.save(subscriptionStatus);
        mockMvc.perform(get("/pds/cnit/eutu55/subscription/status/v1")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("userId", USER_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ON"));
        subscriptionStatusRepository.deleteById(SUBSCRIPTION_STATUS_ID);
    }
}
