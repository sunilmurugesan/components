package uk.gov.hmrc.eos.eutu55.utils;

public enum ErrorCode {

    REQUEST_VALIDATION_FAILED("10010", "Request failed schema validation"),
    REQUEST_LIMIT_EXCEEDED("10020", "Request exceeds size limit of 5000 records"),
    INVALID_REQUEST("10040", "Invalid request"),

    /*EUTU55 Service application exceptions*/
    EUTU55_EIS_GATEWAY_EXCEPTION("EUTUERR100", "Error while executing EIS gateway service"),
    EUTU55_EIS_NO_RESPONSE_BODY("EUTUERR101", "EIS returned no response body"),
    EUTU55_JSON_PROCESSING_EXCEPTION("EUTUERR300", "Error while serialising request parameters as JSON text");

    private final String description;
    private final String code;

    ErrorCode(String code, String description){
        this.description = description;
        this.code = code;
    }

    public String code() {
        return code;
    }

    public String description() {
        return description;
    }
}
