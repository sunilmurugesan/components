package uk.gov.hmrc.eos.eutu55.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmrc.eos.eutu55.model.StatusResultResponse;
import uk.gov.hmrc.eos.eutu55.service.StatusService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/pds/cnit/eutu55")
public class StatusController {

    private final StatusService statusService;

    @GetMapping(value = "/status/v1")
    ResponseEntity<StatusResultResponse> enquireStatus(@RequestHeader(name="userId") String userId){
        StatusResultResponse statusResultResponse = statusService.enquire(userId);;
        return ResponseEntity.ok(statusResultResponse);
    }
}
