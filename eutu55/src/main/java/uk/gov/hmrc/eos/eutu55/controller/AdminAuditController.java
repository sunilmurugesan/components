package uk.gov.hmrc.eos.eutu55.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmrc.eos.eutu55.model.AdminAuditRequest;
import uk.gov.hmrc.eos.eutu55.model.AdminAuditResponse;
import uk.gov.hmrc.eos.eutu55.service.AdminAuditService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/pds/cnit/eutu55")
public class AdminAuditController {

    private final AdminAuditService adminAuditService;

    @PostMapping(value = "/admin/audit/v1")
    ResponseEntity<AdminAuditResponse> auditAdminActions(@RequestBody AdminAuditRequest request){
        log.info("Request for Admin audit data with Admin actions "+ request.getActions() +" and outcomes "+request.getOutcomes() +" for page number "+ request.getPageNumber());
        return ResponseEntity.ok(adminAuditService.getAdminAudits(request));
    }
}
