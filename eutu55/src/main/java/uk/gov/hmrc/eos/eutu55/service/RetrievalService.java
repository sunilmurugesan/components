package uk.gov.hmrc.eos.eutu55.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmrc.eos.eutu55.model.RetrievalRequest;
import uk.gov.hmrc.eos.eutu55.model.RetrievalResultResponse;
import uk.gov.hmrc.eos.eutu55.utils.Outcome;
import uk.gov.hmrc.eu.eutu55.retrieval.RetrievalResponse;

import static uk.gov.hmrc.eos.eutu55.utils.AdminActionType.RETRIEVAL;
import static uk.gov.hmrc.eos.eutu55.utils.SubscriptionStatusType.RECOVERY;

@Service
@Slf4j
@RequiredArgsConstructor
public class RetrievalService {

    @Value("${integration.eis.endpoint.retrieval}")
    private String endpoint;

    @Value("${integration.eis.endpoint.retrieval-bearer-token}")
    private String bearerToken;

    private final SubscriptionStatusService subscriptionStatusService;
    private final AdminAuditService adminAuditService;
    private final GatewayService gatewayService;

    public RetrievalResultResponse processRetrieval(RetrievalRequest request, String userId) {
        RetrievalResponse eisResponse = gatewayService.post(endpoint, request.transform(), RetrievalResponse.class, bearerToken);
        log.info("Retrieval response for from date {} is {}", request.getFromDate(), eisResponse.getOperationResult().getOutcome());
        return saveRetrieval(eisResponse, userId);
    }

    private RetrievalResultResponse saveRetrieval(RetrievalResponse retrievalResponse, String userId) {
        final Outcome outcome = Outcome.valueOf(retrievalResponse.getOperationResult().getOutcome());
        if (outcome.isAccepted()) {
            subscriptionStatusService.saveOrUpdateStatus(RECOVERY);
        }
        adminAuditService.save(RETRIEVAL, outcome, userId);
        return new RetrievalResultResponse(outcome);
    }
}