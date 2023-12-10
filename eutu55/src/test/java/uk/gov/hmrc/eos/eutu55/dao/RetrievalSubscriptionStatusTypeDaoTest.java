package uk.gov.hmrc.eos.eutu55.dao;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import uk.gov.hmrc.eos.eutu55.entity.SubscriptionStatus;
import uk.gov.hmrc.eos.eutu55.repository.SubscriptionStatusRepository;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static uk.gov.hmrc.eos.eutu55.utils.SubscriptionStatusType.RECOVERY;

@DataJpaTest
public class RetrievalSubscriptionStatusTypeDaoTest {

    private SubscriptionStatusRepository subscriptionStatusRepository;

    @Autowired
    public RetrievalSubscriptionStatusTypeDaoTest(SubscriptionStatusRepository subscriptionStatusRepository) {
        this.subscriptionStatusRepository = subscriptionStatusRepository;
    }

    @Test
    @DisplayName("Create SubscriptionStatusTab record for RECOVERY admin action successfully")
    void subscriptionStatusCanBeUpdatedSuccessfully() {
        SubscriptionStatus statusTab = SubscriptionStatus.builder().id(1).status(RECOVERY).requestTime(LocalDateTime.now()).build();
        SubscriptionStatus savedRecord = subscriptionStatusRepository.save(statusTab);
        assertThat(savedRecord.getStatus(), equalTo(RECOVERY));
        assertThat(savedRecord.getRequestTime(), equalTo(statusTab.getRequestTime()));
    }

}
