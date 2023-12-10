package uk.gov.hmrc.eos.eutu55.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmrc.eos.eutu55.service.NotificationService;
import uk.gov.hmrc.eu.eutu55.notification.SyncNotificationReqMsg;
import uk.gov.hmrc.eu.eutu55.notification.SyncNotificationRespMsg;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/pds/cnit/eutu55")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping(value = "/notificationcbs/v1", produces = MediaType.APPLICATION_XML_VALUE,
            consumes = MediaType.APPLICATION_XML_VALUE)
    ResponseEntity<SyncNotificationRespMsg> notify(@RequestBody final SyncNotificationReqMsg request) {
        return ResponseEntity.ok().body(notificationService.saveNotification(request));
    }
}
