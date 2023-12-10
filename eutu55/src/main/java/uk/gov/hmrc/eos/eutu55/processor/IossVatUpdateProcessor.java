package uk.gov.hmrc.eos.eutu55.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import uk.gov.hmrc.eos.eutu55.dao.SynchronisationDao;
import uk.gov.hmrc.eos.eutu55.model.IossVat;
import uk.gov.hmrc.eos.eutu55.utils.OperationType;

import static uk.gov.hmrc.eos.eutu55.utils.OperationType.UPDATE;

@Component
public class IossVatUpdateProcessor extends IossVatProcessor {

    @Autowired
    public IossVatUpdateProcessor(@Lazy final IossVatProcessor iossVatCreateProcessor,
                                  final SynchronisationDao synchronisationDao) {
        super(iossVatCreateProcessor, synchronisationDao);
    }

    @Override
    protected void persist(IossVat iossVat) {
        synchronisationDao.update(iossVat);
    }

    @Override
    protected boolean condition(IossVat iossVat) {
        return super.condition(iossVat) && existsInPds(iossVat.getIossVatId());
    }

    @Override
    public OperationType operationType() {
        return UPDATE;
    }
}
