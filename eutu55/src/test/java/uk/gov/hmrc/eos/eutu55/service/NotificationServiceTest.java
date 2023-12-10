package uk.gov.hmrc.eos.eutu55.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmrc.eos.eutu55.entity.AdminAudit;
import uk.gov.hmrc.eos.eutu55.utils.AdminActionType;
import uk.gov.hmrc.eos.eutu55.utils.Outcome;
import uk.gov.hmrc.eos.eutu55.utils.SubscriptionStatusType;
import uk.gov.hmrc.eu.eutu55.notification.SyncNotificationReqMsg;
import uk.gov.hmrc.eu.eutu55.notification.SyncNotificationType;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmrc.eos.eutu55.utils.AdminActionType.NOTIFICATION;
import static uk.gov.hmrc.eos.eutu55.utils.Outcome.ACCEPTED;
import static uk.gov.hmrc.eos.eutu55.utils.Outcome.REJECTED;
import static uk.gov.hmrc.eos.eutu55.utils.SubscriptionStatusType.ON;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private static final int EXPECTED_NUMBER_OF_INVOCATIONS = 1;
    private static final long TOTAL_ITEMS = 10;
    private static final String USER_ID = "TEST_USER";

    @Captor
    ArgumentCaptor<Object> payloadArgumentCaptor;
    @Captor
    ArgumentCaptor<AdminActionType> adminActionTypeArgumentCaptor;
    @Captor
    ArgumentCaptor<Outcome> outcomeArgumentCaptor;
    @Captor
    ArgumentCaptor<SubscriptionStatusType> statusCaptor;
    @Mock
    private SubscriptionStatusService subscriptionStatusService;
    @Mock
    private AdminAuditService adminAuditService;

    @InjectMocks
    private NotificationService service;

    @Test
    @DisplayName("Notification request with status success should be saved successfully in Admin Audit table and Subscription status should be updated to ON")
    void createNotificationSuccessfullyWithStatusOn() {
        SyncNotificationReqMsg notificationMessage = getNotificationMessage(ACCEPTED.getValue());
        AdminAudit adminAudit = getAdminAudit(notificationMessage);

        when(adminAuditService.save(any(), any(AdminActionType.class), any(Outcome.class))).thenReturn(adminAudit);

        service.saveNotification(notificationMessage);

        verify(adminAuditService, times(EXPECTED_NUMBER_OF_INVOCATIONS)).save(any(NotificationService.NotificationRequestExtract.class), any(AdminActionType.class), any(Outcome.class));
        verify(subscriptionStatusService, times(EXPECTED_NUMBER_OF_INVOCATIONS)).saveOrUpdateStatus(any(SubscriptionStatusType.class));
        verify(adminAuditService).save(payloadArgumentCaptor.capture(), adminActionTypeArgumentCaptor.capture(), outcomeArgumentCaptor.capture());
        verify(subscriptionStatusService).saveOrUpdateStatus(statusCaptor.capture());

        assertThat(statusCaptor.getValue(), equalTo(ON));
        assertThat(adminActionTypeArgumentCaptor.getValue(), equalTo(adminAudit.getAction()));
        assertThat(((NotificationService.NotificationRequestExtract) payloadArgumentCaptor.getValue()).getTotalItems(), equalTo(TOTAL_ITEMS));
        assertThat(outcomeArgumentCaptor.getValue(), equalTo(ACCEPTED));
    }


    @Test
    @DisplayName("Notification request with status fail should be saved successfully in Admin Audit table and no update to Subscription status")
    void createNotificationSuccessfullyWithStatusOff() {
        LocalDateTime disseminationDateTime = LocalDateTime.now();
        SyncNotificationReqMsg notificationMessage = getNotificationMessage(REJECTED.getValue(), disseminationDateTime);
        AdminAudit adminAudit = getAdminAudit(notificationMessage);

        when(adminAuditService.save(any(), any(AdminActionType.class), any(Outcome.class))).thenReturn(adminAudit);

        service.saveNotification(notificationMessage);

        verify(adminAuditService, times(EXPECTED_NUMBER_OF_INVOCATIONS)).save(any(NotificationService.NotificationRequestExtract.class), any(AdminActionType.class), any(Outcome.class));
        verify(subscriptionStatusService, times(EXPECTED_NUMBER_OF_INVOCATIONS)).saveOrUpdateStatus(any(SubscriptionStatusType.class));
        verify(adminAuditService).save(payloadArgumentCaptor.capture(), adminActionTypeArgumentCaptor.capture(), outcomeArgumentCaptor.capture());
        assertThat(adminActionTypeArgumentCaptor.getValue(), equalTo(adminAudit.getAction()));
        assertThat(((NotificationService.NotificationRequestExtract) payloadArgumentCaptor.getValue()).getLastDisseminationDateTime(), equalTo(disseminationDateTime));
        assertThat(outcomeArgumentCaptor.getValue(), equalTo(REJECTED));

    }

    private SyncNotificationReqMsg getNotificationMessage(short status, LocalDateTime lastDisseminationDateTime) {
        SyncNotificationReqMsg requestMsg = new SyncNotificationReqMsg();
        SyncNotificationType type = new SyncNotificationType();
        type.setStatus(status);
        type.setTotalItems(TOTAL_ITEMS);
        type.setLastDisseminationDateTime(lastDisseminationDateTime);
        requestMsg.setSyncNotificationType(type);
        return requestMsg;
    }

    private SyncNotificationReqMsg getNotificationMessage(short status) {
        return getNotificationMessage(status, LocalDateTime.now());
    }

    private AdminAudit getAdminAudit(SyncNotificationReqMsg notificationRequest) {
        Outcome outcome = Outcome.valueOf(notificationRequest.getSyncNotificationType().getStatus());
        return AdminAudit.buildAdminAudit(NOTIFICATION, outcome, USER_ID);
    }


}
