package uk.gov.hmrc.eos.eutu55.dao;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import uk.gov.hmrc.eos.eutu55.entity.AdminAudit;
import uk.gov.hmrc.eos.eutu55.model.AdminAuditRequest;
import uk.gov.hmrc.eos.eutu55.repository.AdminAuditRepository;
import uk.gov.hmrc.eos.eutu55.utils.AdminActionType;
import uk.gov.hmrc.eos.eutu55.utils.Outcome;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Repository
@RequiredArgsConstructor
public class AdminAuditDao {

    private static final int RECORD_LIMIT = 10;
    private static final String SELECT_BY_REQUEST_TIME_SQL = "select ID from ADMIN_AUDIT_TAB where REQUEST_TIME < ?";
    private static final String DELETE_BY_AUDIT_DATE_SQL = "delete from ADMIN_AUDIT_TAB where REQUEST_TIME < ?";

    private final JdbcTemplate jdbcTemplate;
    private final AdminAuditRepository repository;

    public AdminAudit save(AdminAudit adminAudit) {
        return repository.save(adminAudit);
    }

    public Page<AdminAudit> filterBy(AdminAuditRequest request) {
        Pageable page = PageRequest.of(page(request.getPageNumber()), RECORD_LIMIT, Sort.by("requestTime").descending());
        Specification<AdminAudit> spec = buildSpecification(request);
        return repository.findAll(spec, page);
    }

    private Specification<AdminAudit> buildSpecification(AdminAuditRequest request) {
        Specification<AdminAudit> spec = Specification.where(null);
        spec = withDate(spec, request.getDate());
        spec = withActions(spec, request.getActions());
        spec = withOutcomes(spec, request.getOutcomes());
        return spec;
    }

    private Specification<AdminAudit> withDate(Specification<AdminAudit> spec, LocalDate date) {
        if (Objects.nonNull(date)) {
            spec = spec.and((root, query, cb) -> cb.between(root.get("requestTime"), date.atStartOfDay(), date.atTime(LocalTime.MAX)));
        }
        return spec;
    }

    private Specification<AdminAudit> withActions(Specification<AdminAudit> spec, List<AdminActionType> actions) {
        if (Objects.nonNull(actions) && !(actions.isEmpty())) {
            return spec.and((root, query, builder) -> root.get("action").in(actions)
            );
        }
        return spec;
    }

    private Specification<AdminAudit> withOutcomes(Specification<AdminAudit> spec, List<Outcome> outcomes) {
        if (Objects.nonNull(outcomes) && !(outcomes.isEmpty())) {
            return spec.and((root, query, builder) -> root.get("responseOutcome").in(outcomes));
        }
        return spec;
    }

    private int page(Integer offset) {
        return offset == null || offset <= 0 ? 0 : offset - 1;
    }

    public void deleteByRequestTime(LocalDateTime requestTime) {
        log.info("Deleting Admin audit records which are older than {}", requestTime);
        if (requestTime != null) {
            Object[] requestTimeArgs = new Object[]{Timestamp.valueOf(requestTime)};
            List<String> adminAuditIds = jdbcTemplate.queryForList(SELECT_BY_REQUEST_TIME_SQL, String.class, requestTimeArgs);
            // This is required to avoid SQL Warning Code: -1100, SQLState: 02000
            if (!adminAuditIds.isEmpty()) {
                log.info("Deleting records {} from ADMIN_AUDIT_TAB table", adminAuditIds);
                int deleteCount = jdbcTemplate.update(DELETE_BY_AUDIT_DATE_SQL, requestTimeArgs);
                log.info("Deleted {} Admin audit records", deleteCount);
            }
        }
    }
}
