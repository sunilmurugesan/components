package uk.gov.hmrc.eos.eutu55.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@Getter
public class AdminAuditResponse implements Serializable {
    private long recordCount;
    private List<AdminAuditDTO> adminActions;
}