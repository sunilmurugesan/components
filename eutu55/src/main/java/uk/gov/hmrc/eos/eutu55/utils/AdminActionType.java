package uk.gov.hmrc.eos.eutu55.utils;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum AdminActionType {
    RETRIEVAL("retrieval"),
    SUBSCRIPTION("subscription"),
    PING("ping"),
    STATUS("status"),
    NOTIFICATION("notification");

    @JsonValue
    private String value;

    AdminActionType(String value) {
        this.value = value;
    }

}
