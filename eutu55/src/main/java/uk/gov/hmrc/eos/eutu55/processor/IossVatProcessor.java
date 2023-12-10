package uk.gov.hmrc.eos.eutu55.processor;

import lombok.RequiredArgsConstructor;
import uk.gov.hmrc.eos.eutu55.dao.SynchronisationDao;
import uk.gov.hmrc.eos.eutu55.model.IossVat;
import uk.gov.hmrc.eos.eutu55.utils.OperationType;

@RequiredArgsConstructor
public abstract class IossVatProcessor {

    private final IossVatProcessor iossVatProcessor;
    protected final SynchronisationDao synchronisationDao;

    abstract protected void persist(IossVat iossVat);
    abstract public OperationType operationType();

    protected boolean condition(IossVat iossVat) {
        return operationType() == iossVat.getOperation();
    }

    public void process(IossVat iossVat) {
        if(condition(iossVat)) {
            persist(iossVat);
            return;
        }
        if(iossVatProcessor != null) {
            iossVatProcessor.persist(iossVat);
        }
    }

    protected boolean existsInPds(String iossVatId) {
        return synchronisationDao.iossVatExists(iossVatId);
    }
}
