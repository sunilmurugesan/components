package uk.gov.hmrc.eos.eutu55.helper;

import org.junit.jupiter.api.Test;
import uk.gov.hmrc.eos.eutu55.model.IossVat;
import uk.gov.hmrc.eos.eutu55.utils.OperationType;
import uk.gov.hmrc.eu.eutu55.synchronisation.PublishOperationType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

class IossVatBuilderTest {

    private static final String VAT_ID = "IM0000000001";
    private static final String OPERATION_CREATE = "C";
    private static final String VAT_ID_2 = "IM0000000002";

    @Test
    void testBuildFromTypeWithNullDates() {
        PublishOperationType type = new PublishOperationType();
        type.setOperation(OPERATION_CREATE);
        type.setIossVatId(VAT_ID);
        IossVat iossVat = IossVatBuilder.buildFrom(type);

        assertThat(iossVat.getIossVatId(), equalTo(VAT_ID));
        assertThat(iossVat.getOperation(), is(OperationType.CREATE));
        assertThat(iossVat.getValidityStartDate(), nullValue());
        assertThat(iossVat.getValidityEndDate(), nullValue());
        assertThat(iossVat.getEuModificationDateTime(), nullValue());
    }


    @Test
    void testBuildFromTypeWithDates() {
        LocalDate localDate = LocalDate.now();
        PublishOperationType type = new PublishOperationType();
        type.setOperation(OPERATION_CREATE);
        type.setIossVatId(VAT_ID);
        type.setValidityStartDate(localDate);
        type.setValidityEndDate(localDate);
        type.setModificationDateTime(LocalDateTime.now());
        IossVat iossVat = IossVatBuilder.buildFrom(type);

        assertThat(iossVat.getIossVatId(), equalTo(VAT_ID));
        assertThat(iossVat.getOperation(), is(OperationType.CREATE));
        assertThat(iossVat.getValidityStartDate().getYear(), equalTo(type.getValidityStartDate().getYear()));
        assertThat(iossVat.getValidityStartDate().getMonth(), equalTo(type.getValidityStartDate().getMonth()));
        assertThat(iossVat.getValidityStartDate().getDayOfMonth(), equalTo(type.getValidityStartDate().getDayOfMonth()));
    }

    @Test
    void testPassingEmptyListReturnsEmptyList() {
        ArrayList<PublishOperationType> publishOperationTypes = new ArrayList<>();
        List<IossVat> iossVats = IossVatBuilder.buildFromList(publishOperationTypes);
        assertThat(iossVats.size(), equalTo(publishOperationTypes.size()));
    }

    @Test
    void testPassingListReturnsList() {
        LocalDate date = LocalDate.now();
        PublishOperationType type = new PublishOperationType();
        type.setOperation(OPERATION_CREATE);
        type.setIossVatId(VAT_ID);
        type.setValidityStartDate(date);
        type.setValidityEndDate(date);
        type.setModificationDateTime(LocalDateTime.now());

        PublishOperationType type2 = new PublishOperationType();
        type2.setOperation(OPERATION_CREATE);

        type2.setIossVatId(VAT_ID_2);
        type2.setValidityStartDate(date);
        type2.setValidityEndDate(date);
        type2.setModificationDateTime(LocalDateTime.now());

        List<PublishOperationType> list = new ArrayList<>();
        list.add(type);
        list.add(type2);
        List<IossVat> iossVats = IossVatBuilder.buildFromList(list);
        assertThat(iossVats.size(), equalTo(list.size()));
    }

}
