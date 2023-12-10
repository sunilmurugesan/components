package uk.gov.hmrc.eos.eutu55.utils;

import lombok.Getter;

@Getter
public enum SubscriptionStatusType {
    ON(1),
    OFF(0),
    RECOVERY(2);

    private short value;

    SubscriptionStatusType(int value) {
        this.value = (short) value;
    }

}
