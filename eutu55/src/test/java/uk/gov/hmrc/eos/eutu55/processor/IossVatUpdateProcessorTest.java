package uk.gov.hmrc.eos.eutu55.processor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmrc.eos.eutu55.dao.SynchronisationDao;
import uk.gov.hmrc.eos.eutu55.model.IossVat;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmrc.eos.eutu55.IossVatTestHelper.iossVatUpdateOf;

@ExtendWith(MockitoExtension.class)
class IossVatUpdateProcessorTest {

    @Mock
    SynchronisationDao synchronisationDao;
    @Mock
    IossVatCreateProcessor iossVatCreateProcessor;

    @InjectMocks
    IossVatUpdateProcessor processor;

    @Test
    @DisplayName("IOSS VAT record with update operation can be processed successfully when matching record exist in PDS")
    void updateOperationCanBeProcessedWhenMatchingRecordExistInPDS() {
        IossVat inputRecord = iossVatUpdateOf("IM0000000001");

        when(synchronisationDao.iossVatExists(inputRecord.getIossVatId())).thenReturn(true);
        processor.process(inputRecord);

        verify(synchronisationDao, times(1)).update(inputRecord);
        verify(iossVatCreateProcessor, never()).process(inputRecord);
    }

    @Test
    @DisplayName("IOSS VAT record with update operation can be processed as upsert when no matching record exist in PDS")
    void updateRecordWithNoMatchingRecordInPDSCanBeProcessedAsCreate() {
        IossVat inputRecord = iossVatUpdateOf("IM0000000001");

        when(synchronisationDao.iossVatExists(inputRecord.getIossVatId())).thenReturn(false);
        processor.process(inputRecord);

        verify(synchronisationDao, never()).update(inputRecord);
        verify(iossVatCreateProcessor, times(1)).persist(inputRecord);
    }
}