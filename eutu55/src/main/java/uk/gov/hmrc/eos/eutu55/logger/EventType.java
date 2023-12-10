package uk.gov.hmrc.eos.eutu55.logger;

public enum EventType {
    ENTRY_POINT("Entry Point"),
    INVALID_REQUEST("Invalid Request"),
    GENERATE_FAULT("Fault Response Built"),
    API_CALL("API Call"),
    API_CALL_ERROR("API Call Error"),
    PERSIST_RECORD("Persist Record"),
    EXIT_POINT("Response Sent");

    private final String value;

    EventType(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }

}
