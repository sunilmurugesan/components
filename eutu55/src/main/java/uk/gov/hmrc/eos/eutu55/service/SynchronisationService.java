package uk.gov.hmrc.eos.eutu55.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmrc.eos.eutu55.helper.IossVatBuilder;
import uk.gov.hmrc.eos.eutu55.model.IossVat;
import uk.gov.hmrc.eos.eutu55.processor.IossVatProcessor;
import uk.gov.hmrc.eu.eutu55.synchronisation.AcknowledgementType;
import uk.gov.hmrc.eu.eutu55.synchronisation.PublishNumbersRespMsg;
import uk.gov.hmrc.eu.eutu55.synchronisation.PublishOperationType;

import java.util.List;
import java.util.Set;

import static uk.gov.hmrc.eos.eutu55.utils.Outcome.ACCEPTED;

@Service
@Slf4j
public class SynchronisationService {

    private final Set<IossVatProcessor> processors;
    private final SynchronisationAuditService auditService;

    @Autowired
    public SynchronisationService(final Set<IossVatProcessor> processors,
                                  final SynchronisationAuditService auditService) {
        this.processors = processors;
        this.auditService = auditService;
    }

    @Transactional
    public PublishNumbersRespMsg process(final List<PublishOperationType> publishOperationTypes) {
        log.info("IOSS VAT records received for processing");
        if (!(publishOperationTypes.isEmpty())) {
            List<IossVat> iossVatRecords = IossVatBuilder.buildFromList(publishOperationTypes);
            iossVatRecords.forEach(iossVat -> processors.stream()
                            .filter(processor -> processor.operationType() == iossVat.getOperation())
                            .findFirst().orElseThrow()
                            .process(iossVat));
            // Audit logging
            auditService.audit(iossVatRecords);
        }
        log.info("IOSS VAT records processed successfully");
        return getPublishNumbersResponseMessage();
    }

    private PublishNumbersRespMsg getPublishNumbersResponseMessage() {
        PublishNumbersRespMsg type = new PublishNumbersRespMsg();
        AcknowledgementType ack = new AcknowledgementType();
        type.setPublicationResults(ack);
        ack.setOutcome(ACCEPTED.getValue());
        return type;
    }

}
