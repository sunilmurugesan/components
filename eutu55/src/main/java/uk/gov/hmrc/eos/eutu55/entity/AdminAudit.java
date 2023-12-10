package uk.gov.hmrc.eos.eutu55.entity;

import lombok.Getter;
import lombok.Setter;
import uk.gov.hmrc.eos.eutu55.utils.AdminActionType;
import uk.gov.hmrc.eos.eutu55.utils.Outcome;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

import static org.apache.commons.lang3.StringUtils.SPACE;

@Entity
@Table(name = "ADMIN_AUDIT_TAB")
@SequenceGenerator(name = "adminAuditTabSequence", sequenceName = "ADMIN_AUDIT_TAB_SEQ", allocationSize = 1)
@Getter
@Setter
public class AdminAudit implements Serializable {

    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "adminAuditTabSequence")
    private Long id;

    @Column(name = "ACTION", nullable = false)
    @Enumerated(EnumType.STRING)
    private AdminActionType action;

    @Column(name = "EU_PAYLOAD", length = 250)
    private String euPayload;

    @Column(name = "RESPONSE_OUTCOME", nullable = false)
    @Convert(converter = OutcomeConverter.class)
    private Outcome responseOutcome;

    @Column(name = "REQUEST_TIME", columnDefinition = "TIMESTAMP", nullable = false, updatable = false)
    private LocalDateTime requestTime;

    @Column(name= "USER_ID", nullable = false)
    private String userId;

    public static AdminAudit buildAdminAudit(AdminActionType actionType, Outcome outcome, String userId) {
        AdminAudit adminAudit = new AdminAudit();
        adminAudit.setAction(actionType);
        adminAudit.setResponseOutcome(outcome);
        adminAudit.setRequestTime(LocalDateTime.now());
        adminAudit.setUserId(userId);
        return adminAudit;
    }

    public static AdminAudit buildAdminAuditForInboundCall(AdminActionType actionType, Outcome outcome, String euPayloadExtract) {
        String userId = SPACE;
        AdminAudit adminAudit = buildAdminAudit(actionType, outcome, userId);
        adminAudit.setEuPayload(euPayloadExtract);
        return adminAudit;
    }
}
