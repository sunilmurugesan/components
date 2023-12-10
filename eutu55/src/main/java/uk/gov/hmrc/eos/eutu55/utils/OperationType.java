package uk.gov.hmrc.eos.eutu55.utils;

import java.util.Arrays;

public enum OperationType {
    CREATE("C"),
    DELETE("D"),
    UPDATE("U");

    String operation;

    OperationType(String operation) {
        this.operation = operation;
    }

    public String operation() {
        return operation;
    }

    public static OperationType operationType(String operation) {
        return Arrays.stream(OperationType.values()).filter(o -> o.operation().equals(operation)).findFirst()
                .orElseThrow(UnsupportedOperationException::new);
    }
}
