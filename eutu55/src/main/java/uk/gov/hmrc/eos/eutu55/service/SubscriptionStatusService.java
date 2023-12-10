package uk.gov.hmrc.eos.eutu55.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmrc.eos.eutu55.entity.SubscriptionStatus;
import uk.gov.hmrc.eos.eutu55.model.SubscriptionRequest;
import uk.gov.hmrc.eos.eutu55.model.SubscriptionResponse;
import uk.gov.hmrc.eos.eutu55.model.SubscriptionStatusResponse;
import uk.gov.hmrc.eos.eutu55.repository.SubscriptionStatusRepository;
import uk.gov.hmrc.eos.eutu55.utils.Outcome;
import uk.gov.hmrc.eos.eutu55.utils.SubscriptionStatusType;
import uk.gov.hmrc.eu.eutu55.subscription.UpdateSubscriptionReqMsg;
import uk.gov.hmrc.eu.eutu55.subscription.UpdateSubscriptionRespMsg;

import java.time.LocalDateTime;
import java.util.Optional;

import static uk.gov.hmrc.eos.eutu55.utils.AdminActionType.SUBSCRIPTION;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubscriptionStatusService {

    public static final Integer SUBSCRIPTION_STATUS_ID = 1;

    @Value("${integration.eis.endpoint.subscription}")
    private String endpoint;

    @Value("${integration.eis.endpoint.subscription-bearer-token}")
    private String bearerToken;

    private final SubscriptionStatusRepository subscriptionStatusRepository;
    private final GatewayService gatewayService;
    private final AdminAuditService adminAuditService;


    public SubscriptionStatus saveOrUpdateStatus(final SubscriptionStatusType subscriptionStatusType) {
        Optional<SubscriptionStatus> statusRecord = subscriptionStatusRepository.findById(SUBSCRIPTION_STATUS_ID);
        SubscriptionStatus status = statusRecord.orElseGet(() -> SubscriptionStatus.builder().id(SUBSCRIPTION_STATUS_ID).build());
        status.setStatus(subscriptionStatusType);
        status.setRequestTime(LocalDateTime.now());
        subscriptionStatusRepository.save(status);
        return status;
    }

    public SubscriptionResponse saveOrUpdateStatus(final SubscriptionRequest request, final String userId) {
        UpdateSubscriptionRespMsg eisResponse = gatewayService.post(endpoint, transform(request), UpdateSubscriptionRespMsg.class, bearerToken);
        final Outcome outcome = Outcome.valueOf(eisResponse.getOperationResult().getOutcome());
        log.info("Subscription status outcome is {}", outcome);
        if (outcome.isAccepted()) {
            this.saveOrUpdateStatus(request.getStatus());
        }
        adminAuditService.save(SUBSCRIPTION, outcome, userId);
        return new SubscriptionResponse(outcome);
    }

    public SubscriptionStatusResponse retrieveSubscriptionStatus() {
        return subscriptionStatusRepository.findById(SUBSCRIPTION_STATUS_ID).stream()
                .map(ss -> SubscriptionStatusResponse.builder().status(ss.getStatus()).date(ss.getRequestTime()).build())
                .findFirst().orElse(SubscriptionStatusResponse.empty());
    }

    public UpdateSubscriptionReqMsg transform(SubscriptionRequest request) {
        UpdateSubscriptionReqMsg subscriptionRequest = new UpdateSubscriptionReqMsg();
        subscriptionRequest.setNewStatus(request.getStatus().getValue());
        subscriptionRequest.setContactEMail(request.getContactEmail());
        return subscriptionRequest;
    }
}
