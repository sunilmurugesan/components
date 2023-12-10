package uk.gov.hmrc.eos.eutu55.utils;


import uk.gov.hmrc.eu.eutu55.synchronisation.PublishOperationType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RequestTestUtil {

    private static final int END_DATE_DAYS = 5;

    public static PublishOperationType createRecordOf(String iossVatId) {
        return iossVatRecordOf(iossVatId, OperationType.CREATE.operation());
    }

    public static PublishOperationType updateRecordOf(String iossVatId) {
        return iossVatRecordOf(iossVatId, OperationType.UPDATE.operation());
    }

    public static PublishOperationType deleteRecordOf(String iossVatId) {
        return iossVatRecordOf(iossVatId, OperationType.DELETE.operation());
    }

    private static PublishOperationType iossVatRecordOf(String iossVatTd, String operation) {
        PublishOperationType type = new PublishOperationType();
        type.setOperation(operation);
        type.setIossVatId(iossVatTd);
        type.setValidityStartDate(LocalDate.now());
        type.setValidityEndDate(LocalDate.now().plusDays(END_DATE_DAYS));
        LocalDateTime dateTime = LocalDateTime.now();
        type.setModificationDateTime(dateTime);
        return type;
    }
}
