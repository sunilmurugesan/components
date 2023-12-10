package uk.gov.hmrc.eos.eutu55.service;

import static uk.gov.hmrc.eos.eutu55.utils.AdminActionType.PING;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmrc.eos.eutu55.model.PingStatusResponse;
import uk.gov.hmrc.eos.eutu55.utils.Outcome;
import uk.gov.hmrc.eos.eutu55.utils.Status;
import uk.gov.hmrc.eu.eutu55.ping.PingResponse;


@Service
@Slf4j
@RequiredArgsConstructor
public class PingService {

    @Value("${integration.eis.endpoint.ping}")
    private String endpoint;

    @Value("${integration.eis.endpoint.ping-bearer-token}")
    private String bearerToken;

    private final GatewayService gatewayService;
    private final AdminAuditService adminAuditService;

    public PingStatusResponse ping(String userId) {
         PingResponse eisResponse = gatewayService.get(endpoint, PingResponse.class, bearerToken);
        final Status status = Status.valueOf(eisResponse.getOperationResult().getIsAlive());
        log.info("Ping response is {}", status);
        adminAuditService.save(PING, Outcome.valueOf(status.getValue()), userId);
        return new PingStatusResponse(status);
    }

}
