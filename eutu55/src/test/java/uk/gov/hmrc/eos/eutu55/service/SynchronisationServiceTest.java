package uk.gov.hmrc.eos.eutu55.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.hamcrest.MockitoHamcrest;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmrc.eos.eutu55.processor.IossVatCreateProcessor;
import uk.gov.hmrc.eos.eutu55.processor.IossVatDeleteProcessor;
import uk.gov.hmrc.eos.eutu55.processor.IossVatUpdateProcessor;
import uk.gov.hmrc.eu.eutu55.synchronisation.PublishNumbersRespMsg;
import uk.gov.hmrc.eu.eutu55.synchronisation.PublishOperationType;

import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmrc.eos.eutu55.utils.OperationType.CREATE;
import static uk.gov.hmrc.eos.eutu55.utils.OperationType.DELETE;
import static uk.gov.hmrc.eos.eutu55.utils.OperationType.UPDATE;
import static uk.gov.hmrc.eos.eutu55.utils.Outcome.ACCEPTED;
import static uk.gov.hmrc.eos.eutu55.utils.RequestTestUtil.createRecordOf;
import static uk.gov.hmrc.eos.eutu55.utils.RequestTestUtil.deleteRecordOf;
import static uk.gov.hmrc.eos.eutu55.utils.RequestTestUtil.updateRecordOf;

@ExtendWith(MockitoExtension.class)
class SynchronisationServiceTest {

    @Mock
    private IossVatCreateProcessor createProcessor;
    @Mock
    private IossVatUpdateProcessor updateProcessor;
    @Mock
    private IossVatDeleteProcessor deleteProcessor;
    @Mock
    private SynchronisationAuditService auditService;

    private SynchronisationService service;

    @BeforeEach
    void setUp() {
        when(createProcessor.operationType()).thenReturn(CREATE);
        when(updateProcessor.operationType()).thenReturn(UPDATE);
        when(deleteProcessor.operationType()).thenReturn(DELETE);

        service = new SynchronisationService(Set.of(createProcessor, updateProcessor, deleteProcessor), auditService);
    }

    @Test
    @DisplayName("IOSS VAT records with combination of create, update and delete operations are successfully processed for persisting in PDS")
    void inputIossVatRecordsAreSuccessfullyProcessedForPersistingInPDS() {
        List<PublishOperationType> records = List.of(
                createRecordOf("IM0000000001"),
                updateRecordOf("IM0000000002"),
                deleteRecordOf("IM0000000003"));

        PublishNumbersRespMsg response = service.process(records);

        assertThat(response.getPublicationResults().getOutcome(), is(ACCEPTED.getValue()));

        verify(createProcessor, times(1))
                .process(MockitoHamcrest.argThat(hasProperty("iossVatId", is("IM0000000001"))));
        verify(updateProcessor, times(1))
                .process(MockitoHamcrest.argThat(hasProperty("iossVatId", is("IM0000000002"))));
        verify(deleteProcessor, times(1))
                .process(MockitoHamcrest.argThat(hasProperty("iossVatId", is("IM0000000003"))));
    }

    @Test
    @DisplayName("IOSS VAT records with duplicate operations are successfully processed for persisting in PDS")
    void duplicateCreateRecordsAreSuccessfullyProcessedForPersistingInPDS() {
        List<PublishOperationType> records = List.of(
                createRecordOf("IM0000000001"),
                updateRecordOf("IM0000000001"),
                createRecordOf("IM0000000001"),
                deleteRecordOf("IM0000000001"),
                updateRecordOf("IM0000000001"),
                createRecordOf("IM0000000002"));

        PublishNumbersRespMsg response = service.process(records);

        assertThat(response.getPublicationResults().getOutcome(), is(ACCEPTED.getValue()));

        verify(createProcessor, times(2))
                .process(MockitoHamcrest.argThat(hasProperty("iossVatId", is("IM0000000001"))));
        verify(createProcessor, times(1))
                .process(MockitoHamcrest.argThat(hasProperty("iossVatId", is("IM0000000002"))));
        verify(updateProcessor, times(2))
                .process(MockitoHamcrest.argThat(hasProperty("iossVatId", is("IM0000000001"))));
        verify(deleteProcessor, times(1))
                .process(MockitoHamcrest.argThat(hasProperty("iossVatId", is("IM0000000001"))));

    }

}

