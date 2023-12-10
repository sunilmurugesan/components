package uk.gov.hmrc.eos.eutu55.housekeeping;


import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.support.CronTrigger;

import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Slf4j
public class HousekeepingSchedulerTestHarness {

    @Test
    public void schedulerTestHarness(){
        CronTrigger trigger = new CronTrigger("0 0 8 1 1/3 *"); // 0 0 8 1 1/3 *

        Clock clock = Clock.fixed(Instant.parse("2023-06-26T15:46:59.00Z"), ZoneId.of("GMT"));

        Date date = Date.from(clock.instant());
        log.info("Scheduler start date is : {}", format(date));
        //assertThat(format(date), equalTo("29/01/2023 10:09:59 Sunday"));

        Date nextExecutionDate = trigger.nextExecutionTime(triggerContext(date));
        log.info("1st Execution date: {}", format(nextExecutionDate));
        //assertThat(format(nextExecutionDate), equalTo("01/04/2023 00:00:00 Saturday"));

        nextExecutionDate = trigger.nextExecutionTime(triggerContext(nextExecutionDate));
        log.info("2nd Execution date: {}", format(nextExecutionDate));
        //assertThat(format(nextExecutionDate), equalTo("01/07/2023 00:00:00 Saturday"));

        nextExecutionDate = trigger.nextExecutionTime(triggerContext(nextExecutionDate));
        log.info("3rd Execution date: {}", format(nextExecutionDate));
        //assertThat(format(nextExecutionDate), equalTo("01/10/2023 00:00:00 Sunday"));
    }

    private TriggerContext triggerContext(Date date) {
        return new TriggerContext() {
            @Override
            public Date lastScheduledExecutionTime() {
                return date;
            }
            @Override
            public Date lastActualExecutionTime() {
                return date;
            }

            @Override
            public Date lastCompletionTime() {
                return date;
            }
        };
    }

    private String format(Date date) {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss EEEE").format(date);
    }
}
