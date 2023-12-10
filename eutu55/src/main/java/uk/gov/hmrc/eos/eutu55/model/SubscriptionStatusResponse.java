package uk.gov.hmrc.eos.eutu55.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import uk.gov.hmrc.eos.eutu55.utils.SubscriptionStatusType;

import java.time.LocalDateTime;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionStatusResponse {
    private SubscriptionStatusType status;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime date;

    public static SubscriptionStatusResponse empty() {
        return builder().build();
    }
}
