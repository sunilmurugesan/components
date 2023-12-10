package uk.gov.hmrc.eos.eutu55.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmrc.eos.eutu55.utils.SubscriptionStatusType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "SUBSCRIPTION_STATUS_TAB")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionStatus implements Serializable {

    @Id
    @Column(name = "ID", nullable = false)
    private Integer id;

    @Column(name = "LATEST_STATUS", nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscriptionStatusType status;

    @Column(name = "REQUEST_TIME", columnDefinition = "TIMESTAMP", nullable = false)
    private LocalDateTime requestTime;

}
