package uk.gov.hmrc.eos.eutu55.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmrc.eos.eutu55.utils.SubscriptionStatusType;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionRequest {

    @NotNull
    private SubscriptionStatusType status;
    @NotBlank
    @Email(regexp = ".+@.+\\..+")
    private String contactEmail;
}
