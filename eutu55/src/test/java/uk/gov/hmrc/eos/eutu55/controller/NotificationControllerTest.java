package uk.gov.hmrc.eos.eutu55.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmrc.eos.eutu55.exception.EUTU55Exception;
import uk.gov.hmrc.eos.eutu55.logger.LoggerComponent;
import uk.gov.hmrc.eos.eutu55.service.NotificationService;
import uk.gov.hmrc.eu.eutu55.notification.SyncNotificationReqMsg;
import uk.gov.hmrc.eu.eutu55.notification.SyncNotificationRespMsg;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmrc.eos.eutu55.helper.TestHelper.request;
import static uk.gov.hmrc.eos.eutu55.utils.ErrorCode.EUTU55_JSON_PROCESSING_EXCEPTION;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private LoggerComponent logger;

    @Test
    @DisplayName("NotifySynchronisationData endpoint should be invoked and notification service is called")
    void notifySynchronisationData_shouldCallNotificationEndpointAndCallService() throws Exception {

        SyncNotificationRespMsg respMsg = new SyncNotificationRespMsg();
        when(notificationService.saveNotification(any(SyncNotificationReqMsg.class))).thenReturn(respMsg);
        mockMvc.perform(post("/pds/cnit/eutu55/notificationcbs/v1")
                        .content(request("notification/notification-request.xml"))
                        .accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML))
                .andDo(print())
                .andExpect(status().isOk());
        verify(notificationService, times(1)).saveNotification(any(SyncNotificationReqMsg.class));
    }

    @Test
    @DisplayName("NotifySynchronisationData endpoint should respond with correct error when unable to serialise the received payload")
    void notifySynchronisationData_shouldCallNotificationEndpointAndHandleJsonProcessingError() throws Exception {

        doThrow(new EUTU55Exception(EUTU55_JSON_PROCESSING_EXCEPTION)).when(notificationService).saveNotification(any(SyncNotificationReqMsg.class));
        MvcResult mvcResult = mockMvc.perform(post("/pds/cnit/eutu55/notificationcbs/v1")
                        .content(request("notification/notification-request.xml")).accept(MediaType.APPLICATION_XML)
                        .contentType(MediaType.APPLICATION_XML))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andReturn();
        verify(notificationService, times(1)).saveNotification(any(SyncNotificationReqMsg.class));
        assertThat(mvcResult.getResponse().getContentAsString().contains(EUTU55_JSON_PROCESSING_EXCEPTION.code()));
    }

}
