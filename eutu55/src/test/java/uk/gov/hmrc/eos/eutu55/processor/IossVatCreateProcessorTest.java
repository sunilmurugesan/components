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
import static uk.gov.hmrc.eos.eutu55.IossVatTestHelper.iossVatCreateOf;

@ExtendWith(MockitoExtension.class)
class IossVatCreateProcessorTest {

    @Mock
    SynchronisationDao synchronisationDao;
    @Mock
    IossVatUpdateProcessor iossVatUpdateProcessor;

    @InjectMocks
    IossVatCreateProcessor processor;

    @Test
    @DisplayName("IOSS VAT record with create operation can be processed successfully when no matching record exist in PDS")
    void createRecordCanBeSuccessfullyProcessedWhenNoMatchingRecordInPDS() {
        IossVat inputRecord = iossVatCreateOf("IM0000000001");

        when(synchronisationDao.iossVatExists(inputRecord.getIossVatId())).thenReturn(false);
        processor.process(inputRecord);

        verify(synchronisationDao, times(1)).create(inputRecord);
        verify(iossVatUpdateProcessor, never()).process(inputRecord);
    }

    @Test
    @DisplayName("IOSS VAT record with create operation can be processed as update when matching record exist in PDS")
    void createRecordCanBeSuccessfullyProcessedAsUpdateWhenMatchingRecordExistInPDS() {
        IossVat inputRecord = iossVatCreateOf("IM0000000001");

        when(synchronisationDao.iossVatExists(inputRecord.getIossVatId())).thenReturn(true);
        processor.process(inputRecord);

        verify(synchronisationDao, never()).create(inputRecord);
        verify(iossVatUpdateProcessor, times(1)).persist(inputRecord);
    }
}