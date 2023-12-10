package uk.gov.hmrc.eos.eutu55.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmrc.eos.eutu55.utils.AdminActionType;
import uk.gov.hmrc.eos.eutu55.utils.Outcome;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Getter
@Builder
@Jacksonized
public class AdminAuditRequest {
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate date;
    @Builder.Default
    private List<AdminActionType> actions = new ArrayList<>();
    @Builder.Default
    private List<Outcome> outcomes = new ArrayList<>();
    private Integer pageNumber;
}