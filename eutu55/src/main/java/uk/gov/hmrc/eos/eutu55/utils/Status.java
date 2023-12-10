package uk.gov.hmrc.eos.eutu55.utils;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Status {
    UP(1),
    DOWN(0);

    private final short value;

    Status(int value) {
        this.value = (short) value;
    }

    public static Status valueOf(int value) {
        return Arrays.stream(values()).filter(o -> o.value == value).findFirst().orElse(DOWN);
    }
}
