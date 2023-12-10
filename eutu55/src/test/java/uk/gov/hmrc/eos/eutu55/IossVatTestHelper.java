package uk.gov.hmrc.eos.eutu55;

import uk.gov.hmrc.eos.eutu55.model.IossVat;
import uk.gov.hmrc.eos.eutu55.model.IossVatAudit;
import uk.gov.hmrc.eos.eutu55.utils.OperationType;
import uk.gov.hmrc.eos.eutu55.utils.Outcome;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static uk.gov.hmrc.eos.eutu55.utils.OperationType.CREATE;
import static uk.gov.hmrc.eos.eutu55.utils.OperationType.DELETE;
import static uk.gov.hmrc.eos.eutu55.utils.OperationType.UPDATE;
import static uk.gov.hmrc.eos.eutu55.utils.Outcome.ACCEPTED;
import static uk.gov.hmrc.eos.eutu55.utils.Outcome.REJECTED;

public class IossVatTestHelper {

    public static IossVatAudit iossVatAuditCreateRecordOf(String iossVatId) {
        return iossVatAuditRecordOf(iossVatId, CREATE, ACCEPTED);
    }

    public static IossVatAudit iossVatAuditUpdateRecordOf(String iossVatId) {
        return iossVatAuditRecordOf(iossVatId, UPDATE, ACCEPTED);
    }

    public static IossVatAudit iossVatAuditDeleteRecordOf(String iossVatId) {
        return iossVatAuditRecordOf(iossVatId, DELETE, ACCEPTED);
    }

    public static IossVatAudit iossVatAuditCreateRejectRecordOf(String iossVatId) {
        return iossVatAuditRecordOf(iossVatId, CREATE, REJECTED);
    }

    private static IossVatAudit iossVatAuditRecordOf(String iossVatId, OperationType operationType, Outcome outcome) {
        IossVatAudit.IossVatAuditBuilder builder = IossVatAudit.builder();
        IossVat iossVat = null;
        switch (operationType) {
            case CREATE:
                iossVat = iossVatCreateOf(iossVatId);
                break;
            case UPDATE:
                iossVat = iossVatUpdateOf(iossVatId);
                break;
            case DELETE:
                iossVat = iossVatDeleteOf(iossVatId);
                break;
        }
        builder.iossVat(iossVat).operationType(operationType).outcome(outcome);
        if (outcome == REJECTED) {
            builder.remarks("some remarks");
        }
        return builder.build();
    }

    public static IossVat iossVatCreateOf(String iossVatId) {
        return iossVatRecordOf(iossVatId, OperationType.CREATE);
    }

    public static IossVat iossVatUpdateOf(String iossVatId) {
        return iossVatRecordOf(iossVatId, OperationType.UPDATE);
    }

    public static IossVat iossVatDeleteOf(String iossVatId) {
        return iossVatRecordOf(iossVatId, OperationType.DELETE);
    }

    private static IossVat iossVatRecordOf(String iossVatId, OperationType operationType) {
        return IossVat.builder().iossVatId(iossVatId).operation(operationType).validityStartDate(LocalDate.now())
                .validityEndDate(LocalDate.now()).euModificationDateTime(LocalDateTime.now()).build();
    }
}
