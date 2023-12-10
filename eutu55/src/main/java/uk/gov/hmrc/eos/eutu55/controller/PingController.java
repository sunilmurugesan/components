package uk.gov.hmrc.eos.eutu55.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmrc.eos.eutu55.model.PingStatusResponse;
import uk.gov.hmrc.eos.eutu55.service.PingService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/pds/cnit/eutu55")
public class PingController {

    private final PingService pingService;

    @GetMapping(value = "/ping/v1")
    public ResponseEntity<PingStatusResponse> ping(@RequestHeader(name="userId") String userId) {
        return ResponseEntity.ok(pingService.ping(userId));
    }
}
