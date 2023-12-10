package uk.gov.hmrc.eos.eutu55.utils;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Outcome {
    ACCEPTED(1),
    REJECTED(0);

    private final short value;

    Outcome(int value) {
        this.value = (short) value;
    }

    public String getStringValue() {
        return String.valueOf(this.value);
    }

    public static Outcome valueOf(int value) {
        return Arrays.stream(values()).filter(o -> o.value == value).findFirst().orElse(REJECTED);
    }

    public boolean isAccepted() {
        return this == ACCEPTED;
    }
}
