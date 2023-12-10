package uk.gov.hmrc.eos.eutu55.housekeeping;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "housekeeping.enabled", matchIfMissing = true)
public class SchedulerConfiguration {
}
