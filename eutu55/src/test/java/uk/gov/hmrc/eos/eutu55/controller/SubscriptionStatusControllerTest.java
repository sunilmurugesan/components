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
import uk.gov.hmrc.eos.eutu55.model.SubscriptionRequest;
import uk.gov.hmrc.eos.eutu55.model.SubscriptionResponse;
import uk.gov.hmrc.eos.eutu55.model.SubscriptionStatusResponse;
import uk.gov.hmrc.eos.eutu55.service.SubscriptionStatusService;
import uk.gov.hmrc.eos.eutu55.utils.Outcome;
import uk.gov.hmrc.eos.eutu55.utils.SubscriptionStatusType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmrc.eos.eutu55.utils.ErrorCode.INVALID_REQUEST;

@WebMvcTest(SubscriptionStatusController.class)
class SubscriptionStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SubscriptionStatusService subscriptionStatusService;

    @MockBean
    private LoggerComponent logger;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String USER_ID = "TEST_USER";


    @Test
    @DisplayName("subscription/status endpoint should be invoked and subscription status service is called")
    void testRetrieveSubscriptionStatus() throws Exception {
        SubscriptionStatusResponse subscriptionStatusResponse = SubscriptionStatusResponse.builder()
                .status(SubscriptionStatusType.ON)
                .date(LocalDateTime.ofInstant(Instant.ofEpochMilli(1652199929), ZoneId.systemDefault()))
                .build();
        when(subscriptionStatusService.retrieveSubscriptionStatus()).thenReturn(subscriptionStatusResponse);

        mockMvc.perform(get("/pds/cnit/eutu55/subscription/status/v1")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("userId", USER_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ON"))
                .andExpect(jsonPath("$.date").value("20/01/1970 03:56:39"));
        verify(subscriptionStatusService).retrieveSubscriptionStatus();
    }

    @Test
    @DisplayName("Empty record should be returned when subscription status record does not exist in PDS")
    void shouldReturnEmptyRecordWhenSubscriptionStatusDoesNotExist() throws Exception {
        when(subscriptionStatusService.retrieveSubscriptionStatus()).thenReturn(SubscriptionStatusResponse.empty());

        mockMvc.perform(get("/pds/cnit/eutu55/subscription/status/v1")
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("userId", USER_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
        verify(subscriptionStatusService).retrieveSubscriptionStatus();
    }

    @Test
    @DisplayName("subscription endpoint should be invoked and subscription status service is called")
    void testSaveOrUpdateSubscriptionStatus() throws Exception {

        SubscriptionRequest request = new SubscriptionRequest(SubscriptionStatusType.ON, "test@hmrc.gov.uk");

        SubscriptionResponse response = new SubscriptionResponse(Outcome.ACCEPTED);
        when(subscriptionStatusService.saveOrUpdateStatus(any(SubscriptionRequest.class), any())).thenReturn(response);

        mockMvc.perform(post("/pds/cnit/eutu55/subscription/v1")
                .content(objectMapper.writeValueAsString(request))
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("userId", USER_ID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.outcome").value("ACCEPTED"));
        verify(subscriptionStatusService).saveOrUpdateStatus(any(SubscriptionRequest.class), any());
    }

    @Test
    @DisplayName("subscription endpoint should return error when invalid status ia passed in request")
    void testSaveOrUpdateSubscriptionStatus_throwsBadRequest() throws Exception {

        SubscriptionResponse response = new SubscriptionResponse(Outcome.ACCEPTED);
        when(subscriptionStatusService.saveOrUpdateStatus(any(SubscriptionRequest.class), any())).thenReturn(response);

        mockMvc.perform(post("/pds/cnit/eutu55/subscription/v1")
                .content("{\"status\": \"ABC\"," +
                        "    \"contactEmail\": \"test@hmrc.gov.uk\"" +
                        "}")
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .header("userId", USER_ID))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(INVALID_REQUEST.code()));
        verify(subscriptionStatusService, never()).saveOrUpdateStatus(any(SubscriptionRequest.class), any());
    }
}
