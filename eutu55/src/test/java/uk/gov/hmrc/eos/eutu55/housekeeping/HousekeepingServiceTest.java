package uk.gov.hmrc.eos.eutu55.housekeeping;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmrc.eos.eutu55.dao.AdminAuditDao;
import uk.gov.hmrc.eos.eutu55.dao.SynchronisationAuditDao;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(properties = {"housekeeping.frequency=*/2 * * * * *",
        "housekeeping.retention-period=10"})
@Slf4j
public class HousekeepingServiceTest {

    @SpyBean
    HousekeepingService housekeepingService;
    @SpyBean
    AdminAuditDao adminAuditDao;
    @SpyBean
    SynchronisationAuditDao synchronisationAuditDao;

    @Captor
    ArgumentCaptor<LocalDateTime> localDateTimeCaptor;

    private Clock clock = Clock.fixed(Instant.parse("2023-01-29T10:09:59.00Z"), ZoneId.systemDefault());

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(housekeepingService, "clock", clock);
    }

    @Test
    public void houseKeepingCanBePerformedInTheGivenCronFrequency() {
        await().atMost(Duration.of(10, ChronoUnit.SECONDS)).untilAsserted(() -> {
            verify(housekeepingService, atLeast(5)).performHousekeeping();
            verify(synchronisationAuditDao, atLeast(5)).deleteByAuditDate(localDateTimeCaptor.capture());
            verify(adminAuditDao, atLeast(5)).deleteByRequestTime(localDateTimeCaptor.capture());
        });
        assertThat(localDateTimeCaptor.getValue(), is(LocalDateTime.ofInstant(Instant.parse("2023-01-19T00:00:00.00Z"),
                ZoneId.systemDefault())));
    }
}