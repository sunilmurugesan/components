package uk.gov.hmrc.eos.eutu55.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import uk.gov.hmrc.eos.eutu55.dao.SynchronisationAuditDao;
import uk.gov.hmrc.eos.eutu55.model.IossVat;
import uk.gov.hmrc.eos.eutu55.model.IossVatAudit;

import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.hmrc.eos.eutu55.utils.Outcome.ACCEPTED;

@Slf4j
@RequiredArgsConstructor
@Service
public class SynchronisationAuditService {

    private final SynchronisationAuditDao dao;

    @Async
    public void audit(List<IossVat> iossVatRecords) {
        log.info("Start auditing for Dissemination ..");
        List<IossVatAudit> auditRecords = process(iossVatRecords);
        dao.save(auditRecords);
    }

    private List<IossVatAudit> process(List<IossVat> iossVatRecords) {
        log.info("Processing IOSS VAT records for auditing ..");
        return iossVatRecords.stream()
                .map(this::toAudit)
                .collect(Collectors.toList());
    }

    private IossVatAudit toAudit(IossVat vatRecord) {
        return toBuilder(vatRecord).operationType(vatRecord.getOperation()).outcome(ACCEPTED).build();
    }

    private IossVatAudit.IossVatAuditBuilder toBuilder(IossVat r) {
        return IossVatAudit.builder().iossVat(r);
    }
}