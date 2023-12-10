package uk.gov.hmrc.eos.eutu55.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import uk.gov.hmrc.eos.eutu55.dao.AdminAuditDao;
import uk.gov.hmrc.eos.eutu55.entity.AdminAudit;
import uk.gov.hmrc.eos.eutu55.model.AdminAuditDTO;
import uk.gov.hmrc.eos.eutu55.model.AdminAuditRequest;
import uk.gov.hmrc.eos.eutu55.model.AdminAuditResponse;
import uk.gov.hmrc.eos.eutu55.utils.AdminActionType;
import uk.gov.hmrc.eos.eutu55.utils.JsonSerialiser;
import uk.gov.hmrc.eos.eutu55.utils.Outcome;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminAuditService {

    private final AdminAuditDao adminAuditDao;

    public AdminAudit save(final Object payloadExtract, AdminActionType actionType, Outcome outcome) {
        log.info("Admin audit payload save action for action type "+ actionType+ "  and outcome "+ outcome);
        AdminAudit adminAudit = AdminAudit.buildAdminAuditForInboundCall(actionType, outcome, JsonSerialiser.serialise(payloadExtract));
        return adminAuditDao.save(adminAudit);
    }

    public AdminAudit save(AdminActionType actionType, Outcome outcome, String userId) {
        log.info("Admin audit save action for action type {} and outcome {}", actionType, outcome);
        AdminAudit adminAudit = AdminAudit.buildAdminAudit(actionType, outcome, userId);
        return adminAuditDao.save(adminAudit);
    }

    public AdminAuditResponse getAdminAudits(AdminAuditRequest request) {
        Page<AdminAudit> pagedAdminRecords = adminAuditDao.filterBy(request);
        AdminAuditResponse adminAuditResponse = new AdminAuditResponse(pagedAdminRecords.getTotalElements(),
                pagedAdminRecords.getContent().stream().map(AdminAuditDTO::of).collect(Collectors.toList()));
        log.info("Returning {} of {} records for page {} ",
                adminAuditResponse.getRecordCount(), pagedAdminRecords.getTotalElements(), pagedAdminRecords.getNumber() + 1);
        return adminAuditResponse;
    }
}
