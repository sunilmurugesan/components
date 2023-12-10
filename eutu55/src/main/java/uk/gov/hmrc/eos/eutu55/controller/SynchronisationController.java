package uk.gov.hmrc.eos.eutu55.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmrc.eos.eutu55.service.SynchronisationService;
import uk.gov.hmrc.eu.eutu55.synchronisation.PublishNumbersReqMsg;
import uk.gov.hmrc.eu.eutu55.synchronisation.PublishNumbersRespMsg;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/pds/cnit/eutu55")
public class SynchronisationController {
    private final SynchronisationService synchronisationService;

    @PostMapping(value = "/synchronisation/v1", produces = MediaType.APPLICATION_XML_VALUE,
            consumes = MediaType.APPLICATION_XML_VALUE)
    ResponseEntity<PublishNumbersRespMsg> synchroniseIossVatData(@RequestBody final PublishNumbersReqMsg request) {
        log.info("Total records received in synchronisation request - {}", request.getIossVatNumberUpdates().size());
        PublishNumbersRespMsg publishNumbersRespMsg = synchronisationService.process(request.getIossVatNumberUpdates());

        return ResponseEntity.ok().body(publishNumbersRespMsg);
    }
}
