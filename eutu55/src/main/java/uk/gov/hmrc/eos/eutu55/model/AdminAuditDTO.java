package uk.gov.hmrc.eos.eutu55.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import uk.gov.hmrc.eos.eutu55.entity.AdminAudit;
import uk.gov.hmrc.eos.eutu55.utils.AdminActionType;
import uk.gov.hmrc.eos.eutu55.utils.Outcome;

import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@Getter
public class AdminAuditDTO implements Serializable {

    private AdminActionType action;
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private LocalDateTime date;
    private Outcome outcome;
    private String userId;

    public static AdminAuditDTO of(AdminAudit adminAudit) {
        return AdminAuditDTO.builder().action(adminAudit.getAction())
                .date(adminAudit.getRequestTime())
                .outcome(adminAudit.getResponseOutcome())
                .userId(adminAudit.getUserId())
                .build();
    }

}
