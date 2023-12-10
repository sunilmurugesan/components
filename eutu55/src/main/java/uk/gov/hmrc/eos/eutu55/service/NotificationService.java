package uk.gov.hmrc.eos.eutu55.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmrc.eos.eutu55.utils.Outcome;
import uk.gov.hmrc.eu.eutu55.notification.SyncNotificationReqMsg;
import uk.gov.hmrc.eu.eutu55.notification.SyncNotificationRespMsg;

import java.time.LocalDateTime;

import static uk.gov.hmrc.eos.eutu55.utils.AdminActionType.NOTIFICATION;
import static uk.gov.hmrc.eos.eutu55.utils.SubscriptionStatusType.OFF;
import static uk.gov.hmrc.eos.eutu55.utils.SubscriptionStatusType.ON;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
    private final SubscriptionStatusService subscriptionStatusService;

    private final AdminAuditService adminAuditService;

    public SyncNotificationRespMsg saveNotification(SyncNotificationReqMsg notificationRequest) {
        final Outcome outcome = Outcome.valueOf(notificationRequest.getSyncNotificationType().getStatus());
        long totalItems = notificationRequest.getSyncNotificationType().getTotalItems();
        LocalDateTime lastDisseminationDateTime = notificationRequest.getSyncNotificationType().getLastDisseminationDateTime();
        NotificationRequestExtract notificationRequestExtract = new NotificationRequestExtract(totalItems, lastDisseminationDateTime);
        log.info("Notification received for retrieval with outcome {}, total items {} and last disseminated date {}",
            outcome, totalItems, lastDisseminationDateTime);
        adminAuditService.save(notificationRequestExtract, NOTIFICATION, outcome);
        subscriptionStatusService.saveOrUpdateStatus(outcome.isAccepted() ? ON : OFF);
        return new SyncNotificationRespMsg();
    }

    /**
     * Nested class representing the Notification Request fields we wish to store in JSON format in the EU_PAYLOAD column.
     * The Admin Audit Service receives an instance of the class and writes the fields out in JSON format.
     * The same technique can be used for any future EUTU55 APIs that need to store data in the EU_PAYLOAD column.
     */
    @Getter
    @AllArgsConstructor
    public class NotificationRequestExtract {
        private long totalItems;
        private LocalDateTime lastDisseminationDateTime;
    }
}
