package uk.gov.hmrc.eos.eutu55.model;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmrc.eos.eutu55.utils.OperationType;
import uk.gov.hmrc.eos.eutu55.utils.Outcome;

@Getter
@Builder
public class IossVatAudit {
    private IossVat iossVat;
    private OperationType operationType;
    private Outcome outcome;
    private String remarks;
}
