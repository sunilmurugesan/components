package uk.gov.hmrc.eos.eutu55.config;

public class RequestCorrelationId {
    public static final String X_CORRELATION_ID = "X-Correlation-ID";
    private static final ThreadLocal<String> requestId = new ThreadLocal<>();

    public static String getRequestId() {
        return requestId.get();
    }

    public static void setRequestId(String correlationId) {
        requestId.set(correlationId);
    }
}