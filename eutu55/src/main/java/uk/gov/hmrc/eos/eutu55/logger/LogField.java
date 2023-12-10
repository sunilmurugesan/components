package uk.gov.hmrc.eos.eutu55.logger;

public enum LogField {
    UPSTREAM_SERVICE_NAME("Upstream_Service.Name"),
    REQUEST_ID("Request_Id"),
    EVENT_ACTION("event.action"),
    EVENT_SEQUENCE("event.sequence"),
    EVENT_TYPE("event.type"),
    REQUEST("request"),
    RESPONSE("response"),
    ERROR_CODE("error.code"),
    ERROR_TEXT("error.text");

    private final String key;

    LogField(String key) {
        this.key = key;
    }

    public String key() {
        return this.key;
    }
}
