package uk.gov.hmrc.eos.eutu55.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmrc.eos.eutu55.model.SubscriptionRequest;
import uk.gov.hmrc.eos.eutu55.model.SubscriptionResponse;
import uk.gov.hmrc.eos.eutu55.model.SubscriptionStatusResponse;
import uk.gov.hmrc.eos.eutu55.service.SubscriptionStatusService;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pds/cnit/eutu55")
public class SubscriptionStatusController {

    private final SubscriptionStatusService subscriptionStatusService;

    @GetMapping(value = "/subscription/status/v1")
    ResponseEntity<SubscriptionStatusResponse> getSubscriptionStatus() {
        SubscriptionStatusResponse subscriptionStatusResponse = subscriptionStatusService.retrieveSubscriptionStatus();
        return ResponseEntity.ok(subscriptionStatusResponse);
    }

    @PostMapping(value = "/subscription/v1")
    ResponseEntity<SubscriptionResponse> updateSubscriptionStatus(@RequestHeader(name = "userId") String userId,
                                                                  @Valid @RequestBody SubscriptionRequest request) {
        SubscriptionResponse subscriptionResponse = subscriptionStatusService.saveOrUpdateStatus(request, userId);
        return ResponseEntity.ok(subscriptionResponse);
    }
}
