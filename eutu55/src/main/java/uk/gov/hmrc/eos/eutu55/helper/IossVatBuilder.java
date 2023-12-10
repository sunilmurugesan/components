package uk.gov.hmrc.eos.eutu55.helper;

import uk.gov.hmrc.eos.eutu55.model.IossVat;
import uk.gov.hmrc.eos.eutu55.utils.OperationType;
import uk.gov.hmrc.eu.eutu55.synchronisation.PublishOperationType;

import java.util.List;
import java.util.stream.Collectors;

public class IossVatBuilder {


    /**
     * This method returns List of IossVat objects for the supplied  List of PublishOperationType objects
     *
     * @param publishOperationTypes {@link List} of {@link PublishOperationType }
     * @return {@link List} of {@link IossVat}
     */
    public static List<IossVat> buildFromList(List<PublishOperationType> publishOperationTypes) {
        return publishOperationTypes.stream().map(IossVatBuilder::buildFrom).collect(Collectors.toList());
    }


    /**
     * This method returns IossVat object for the supplied  PublishOperationType object
     *
     * @param publishOperationType {@link PublishOperationType }
     * @return {@link IossVat}
     */
    static IossVat buildFrom(PublishOperationType publishOperationType) {
        return (publishOperationType != null) ? IossVat.builder()
                .operation(OperationType.operationType(publishOperationType.getOperation()))
                .iossVatId(publishOperationType.getIossVatId())
                .validityStartDate(publishOperationType.getValidityStartDate())
                .validityEndDate(publishOperationType.getValidityEndDate())
                .euModificationDateTime(publishOperationType.getModificationDateTime())
                .build() : null;
    }


}
