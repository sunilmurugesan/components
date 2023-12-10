package uk.gov.hmrc.eos.eutu55.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmrc.eos.eutu55.utils.OperationType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class IossVat {

    private Long id;
    private String iossVatId;
    private OperationType operation;
    private LocalDate validityStartDate;
    private LocalDate validityEndDate;
    private LocalDateTime euModificationDateTime;
    private LocalDateTime createdDateTime;
    private LocalDateTime updatedDateTime;
}
