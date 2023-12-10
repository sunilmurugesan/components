package uk.gov.hmrc.eos.eutu55.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import uk.gov.hmrc.eos.eutu55.dao.SynchronisationDao;
import uk.gov.hmrc.eos.eutu55.model.IossVat;
import uk.gov.hmrc.eos.eutu55.utils.OperationType;

import static uk.gov.hmrc.eos.eutu55.utils.OperationType.CREATE;

@Component
public class IossVatCreateProcessor extends IossVatProcessor {

    @Autowired
    public IossVatCreateProcessor(@Lazy final IossVatProcessor iossVatUpdateProcessor,
                                  final SynchronisationDao synchronisationDao) {
        super(iossVatUpdateProcessor, synchronisationDao);
    }

    @Override
    protected boolean condition(IossVat iossVat) {
        return super.condition(iossVat) && !existsInPds(iossVat.getIossVatId());
    }

    @Override
    protected void persist(IossVat iossVat) {
        synchronisationDao.create(iossVat);
    }

    @Override
    public OperationType operationType() {
        return CREATE;
    }

}
