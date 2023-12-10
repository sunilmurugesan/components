package uk.gov.hmrc.eos.eutu55.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmrc.eos.eutu55.dao.SynchronisationDao;
import uk.gov.hmrc.eos.eutu55.model.IossVat;
import uk.gov.hmrc.eos.eutu55.utils.OperationType;

import static uk.gov.hmrc.eos.eutu55.utils.OperationType.DELETE;

@Component
public class IossVatDeleteProcessor extends IossVatProcessor {

    @Autowired
    public IossVatDeleteProcessor(final SynchronisationDao synchronisationDao) {
        super(null, synchronisationDao);
    }

    @Override
    protected boolean condition(IossVat iossVat) {
        return super.condition(iossVat);
    }

    @Override
    protected void persist(IossVat iossVat) {
        synchronisationDao.delete(iossVat.getIossVatId());
    }

    @Override
    public OperationType operationType() {
        return DELETE;
    }
}
