package uk.gov.hmrc.eos.eutu55.repository;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmrc.eos.eutu55.entity.AdminAudit;

@Repository
public interface AdminAuditRepository extends CrudRepository<AdminAudit, Long>, JpaSpecificationExecutor<AdminAudit> {
}