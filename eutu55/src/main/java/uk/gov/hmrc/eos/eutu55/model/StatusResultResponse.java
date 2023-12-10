package uk.gov.hmrc.eos.eutu55.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import uk.gov.hmrc.eos.eutu55.utils.DateUtil;
import uk.gov.hmrc.eos.eutu55.utils.Outcome;
import uk.gov.hmrc.eu.eutu55.status.StatusResponse;

import java.time.LocalDateTime;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatusResultResponse {
    private Outcome outcome;
    private Integer status;
    private Long totalItems;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime lastDisseminationDateTime;

    public static StatusResultResponse of(StatusResponse eisResponse) {
        final Outcome outcome = Outcome.valueOf(eisResponse.getStatusResult().getOutcome());
        return StatusResultResponse.builder()
                .outcome(outcome)
                .status(eisResponse.getStatus() != null ? eisResponse.getStatus().intValue() : null)
                .totalItems(eisResponse.getTotalItems())
                .lastDisseminationDateTime(DateUtil.localDateTimeFromXmlDate(eisResponse.getLastDisseminationDateTime()))
                .build();
    }
}