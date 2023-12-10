package uk.gov.hmrc.eos.eutu55.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import uk.gov.hmrc.eos.eutu55.entity.SubscriptionStatus;
import uk.gov.hmrc.eos.eutu55.repository.SubscriptionStatusRepository;
import uk.gov.hmrc.eos.eutu55.utils.SubscriptionStatusType;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static uk.gov.hmrc.eos.eutu55.utils.SubscriptionStatusType.OFF;
import static uk.gov.hmrc.eos.eutu55.utils.SubscriptionStatusType.ON;
import static uk.gov.hmrc.eos.eutu55.utils.SubscriptionStatusType.RECOVERY;

@DataJpaTest
public class SubscriptionStatusTypeDaoTest {

    private SubscriptionStatusRepository subscriptionStatusRepository;

    @Autowired
    public SubscriptionStatusTypeDaoTest(SubscriptionStatusRepository subscriptionStatusRepository) {
        this.subscriptionStatusRepository = subscriptionStatusRepository;
    }

    @ParameterizedTest
    @EnumSource(value = SubscriptionStatusType.class, names = {"ON", "OFF", "RECOVERY"})
    @DisplayName("Create SubscriptionStatusTab records for each status successfully")
    void SubscriptionStatusCreatedSuccessfully(SubscriptionStatusType status) {
        SubscriptionStatus statusTab = getSubscriptionStatusTab(status);
        SubscriptionStatus result = subscriptionStatusRepository.save(statusTab);
        Optional<SubscriptionStatus> persistedRecord = subscriptionStatusRepository.findById(result.getId());
        SubscriptionStatus subscriptionStatus = persistedRecord.orElseGet(() -> SubscriptionStatus.builder().build());
        assertThat(result.getId(), equalTo(subscriptionStatus.getId()));
        assertThat(result.getStatus(), equalTo(subscriptionStatus.getStatus()));
    }


    @Test
    @DisplayName("Create and update SubscriptionStatusTab record from RECOVERY to ON successfully")
    void SubscriptionStatusUpdatedToOnSuccessfully() {
        SubscriptionStatus statusTab = getSubscriptionStatusTab(RECOVERY);
        SubscriptionStatus savedRecord = subscriptionStatusRepository.save(statusTab);
        assertThat(savedRecord.getStatus(), equalTo(RECOVERY));
        assertThat(savedRecord.getRequestTime(), equalTo(statusTab.getRequestTime()));
        savedRecord.setStatus(ON);
        SubscriptionStatus updatedRecord = subscriptionStatusRepository.save(savedRecord);
        assertThat(updatedRecord.getStatus(), equalTo(ON));
    }

    @Test
    @DisplayName("Create and update SubscriptionStatusTab record from RECOVERY to OFF successfully")
    void SubscriptionStatusUpdatedToOFFSuccessfully() {
        SubscriptionStatus statusTab = getSubscriptionStatusTab(RECOVERY);
        SubscriptionStatus savedRecord = subscriptionStatusRepository.save(statusTab);
        assertThat(savedRecord.getStatus(), equalTo(RECOVERY));
        assertThat(savedRecord.getRequestTime(), equalTo(statusTab.getRequestTime()));
        savedRecord.setStatus(OFF);
        SubscriptionStatus updatedRecord = subscriptionStatusRepository.save(savedRecord);
        assertThat(updatedRecord.getStatus(), equalTo(OFF));
    }


    private SubscriptionStatus getSubscriptionStatusTab(SubscriptionStatusType status) {
        return SubscriptionStatus.builder().id(1).status(status).requestTime(LocalDateTime.now()).build();
    }

}
