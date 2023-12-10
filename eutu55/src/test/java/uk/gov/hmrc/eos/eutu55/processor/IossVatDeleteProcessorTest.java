package uk.gov.hmrc.eos.eutu55.processor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmrc.eos.eutu55.dao.SynchronisationDao;
import uk.gov.hmrc.eos.eutu55.model.IossVat;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmrc.eos.eutu55.IossVatTestHelper.iossVatDeleteOf;

@ExtendWith(MockitoExtension.class)
class IossVatDeleteProcessorTest {

    @Mock
    SynchronisationDao synchronisationDao;

    @InjectMocks
    IossVatDeleteProcessor processor;

    @Test
    @DisplayName("IOSS VAT record with delete operation can be processed successfully")
    void deleteRecordCanBeProcessedWhenMatchingRecordExistInPDS() {
        IossVat inputRecord = iossVatDeleteOf("IM0000000001");

        processor.process(inputRecord);

        verify(synchronisationDao, times(1)).delete(inputRecord.getIossVatId());
    }
}