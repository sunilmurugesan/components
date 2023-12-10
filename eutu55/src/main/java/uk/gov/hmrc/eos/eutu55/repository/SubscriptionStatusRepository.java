package uk.gov.hmrc.eos.eutu55.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmrc.eos.eutu55.entity.SubscriptionStatus;

@Repository
public interface SubscriptionStatusRepository extends CrudRepository<SubscriptionStatus, Integer> {
}
