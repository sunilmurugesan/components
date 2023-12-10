package uk.gov.hmrc.eos.eutu55.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmrc.eos.eutu55.model.StatusResultResponse;
import uk.gov.hmrc.eu.eutu55.status.StatusResponse;

import static uk.gov.hmrc.eos.eutu55.utils.AdminActionType.STATUS;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatusService {

    @Value("${integration.eis.endpoint.status}")
    private String endpoint;

    @Value("${integration.eis.endpoint.status-bearer-token}")
    private String bearerToken;

    private final GatewayService gatewayService;
    private final AdminAuditService adminAuditService;

    public StatusResultResponse enquire(String userId) {
        StatusResponse eisResponse = gatewayService.get(endpoint, StatusResponse.class, bearerToken);
        StatusResultResponse statusResponse = StatusResultResponse.of(eisResponse);
        log.info("Status result {}", statusResponse.getOutcome());
        adminAuditService.save(STATUS, statusResponse.getOutcome(), userId);
        return statusResponse;
    }
}
