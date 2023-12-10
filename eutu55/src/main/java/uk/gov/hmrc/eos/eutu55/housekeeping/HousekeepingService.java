package uk.gov.hmrc.eos.eutu55.housekeeping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;
import uk.gov.hmrc.eos.eutu55.dao.AdminAuditDao;
import uk.gov.hmrc.eos.eutu55.dao.SynchronisationAuditDao;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;

@Service
@RequiredArgsConstructor
@Slf4j
public class HousekeepingService {

    @Value("${housekeeping.retention-period: 1095}")
    private int retentionPeriodFromConfig;

    @Value("${housekeeping.frequency: 0 0 0 * */3 *}")
    private String houseKeepingCronFrequency;

    private Clock clock = Clock.systemDefaultZone();
    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss EEEE");

    private final AdminAuditDao adminAuditDao;
    private final SynchronisationAuditDao synchronisationAuditDao;

    @Scheduled(cron = "${housekeeping.frequency}")
    @Transactional
    public void performHousekeeping() {
        housekeeping(retentionPeriodFromConfig);
    }

    @Transactional
    public void performHousekeeping(Integer retentionPeriodFromRequest) {
        int retentionPeriod = retentionPeriodFromRequest != null ? retentionPeriodFromRequest : retentionPeriodFromConfig;
        log.info("Retention period of records: {} days", retentionPeriod);
        housekeeping(retentionPeriod);
    }

    private void housekeeping(int retentionPeriod) {
        StopWatch watch = new StopWatch();
        watch.start();
        LocalDateTime now = LocalDateTime.now(clock);
        var instant = clock.instant().minus(Period.ofDays(retentionPeriod));
        var dateForDeletion = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).truncatedTo(ChronoUnit.DAYS);
        log.info("Deleting records older than {}", format(dateForDeletion));
        adminAuditDao.deleteByRequestTime(dateForDeletion);
        synchronisationAuditDao.deleteByAuditDate(dateForDeletion);
        watch.stop();
        log.info("Total time taken to run Housekeeping {} millis", watch.getTotalTimeMillis());
        logNextExecutionTime(now);
    }

    private void logNextExecutionTime(LocalDateTime localDateTime) {
        CronExpression cronExpression = CronExpression.parse(houseKeepingCronFrequency);
        var next = cronExpression.next(localDateTime);
        log.info("*** Next Housekeeping scheduled to start at {} ***", format(next));
    }

    private String format(Temporal temporal) {
        return DATE_TIME_FORMATTER.format(temporal);
    }
}
