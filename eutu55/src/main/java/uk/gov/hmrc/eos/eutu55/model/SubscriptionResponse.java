package uk.gov.hmrc.eos.eutu55.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmrc.eos.eutu55.utils.Outcome;

@AllArgsConstructor
@Getter
public class SubscriptionResponse {
    private Outcome outcome;
}
