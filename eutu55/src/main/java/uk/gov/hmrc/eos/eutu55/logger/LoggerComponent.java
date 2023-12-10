package uk.gov.hmrc.eos.eutu55.logger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import uk.gov.hmrc.eos.eutu55.config.RequestCorrelationId;
import uk.gov.hmrc.eos.eutu55.utils.ErrorCode;
import uk.gov.hmrc.eos.eutu55.utils.XmlMarshaller;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static uk.gov.hmrc.eos.eutu55.logger.EventType.API_CALL;
import static uk.gov.hmrc.eos.eutu55.logger.EventType.API_CALL_ERROR;
import static uk.gov.hmrc.eos.eutu55.logger.EventType.ENTRY_POINT;
import static uk.gov.hmrc.eos.eutu55.logger.EventType.EXIT_POINT;
import static uk.gov.hmrc.eos.eutu55.logger.EventType.GENERATE_FAULT;
import static uk.gov.hmrc.eos.eutu55.logger.EventType.INVALID_REQUEST;
import static uk.gov.hmrc.eos.eutu55.logger.EventType.PERSIST_RECORD;
import static uk.gov.hmrc.eos.eutu55.logger.LogField.ERROR_CODE;
import static uk.gov.hmrc.eos.eutu55.logger.LogField.ERROR_TEXT;
import static uk.gov.hmrc.eos.eutu55.logger.LogField.EVENT_ACTION;
import static uk.gov.hmrc.eos.eutu55.logger.LogField.EVENT_SEQUENCE;
import static uk.gov.hmrc.eos.eutu55.logger.LogField.EVENT_TYPE;
import static uk.gov.hmrc.eos.eutu55.logger.LogField.REQUEST;
import static uk.gov.hmrc.eos.eutu55.logger.LogField.REQUEST_ID;
import static uk.gov.hmrc.eos.eutu55.logger.LogField.RESPONSE;
import static uk.gov.hmrc.eos.eutu55.logger.LogField.UPSTREAM_SERVICE_NAME;

@Slf4j
@EnableConfigurationProperties(LogEventProperties.class)
@RequiredArgsConstructor
@Component
public class LoggerComponent {

    private final LogEventProperties logEventProperties;
    private final HttpServletRequest request;

    public void nextSequence() {
        String sequence = MDC.get(EVENT_SEQUENCE.key());
        String nextSequence = "01";
        if (sequence != null) {
            nextSequence = StringUtils.leftPad(String.valueOf(Integer.parseInt(sequence) + 1), 2, "0");
        }
        MDC.put(EVENT_SEQUENCE.key(), nextSequence);
    }

    public void entryPoint() {
        nextSequence();
        addLogFieldToMDC(EVENT_ACTION.key());
        addLogFieldToMDC(UPSTREAM_SERVICE_NAME.key());
        MDC.put(REQUEST_ID.key(), RequestCorrelationId.getRequestId());
        MDC.put(EVENT_TYPE.key(), ENTRY_POINT.value());
        logEntryPoint();
        clearEntryPoint();
    }

    public void clearEventType() {
        removeFromMDC(EVENT_TYPE.key());
    }

    public void clearEntryPoint() {
        removeFromMDC(EVENT_TYPE.key());
        removeFromMDC(REQUEST.key());
    }

    public void invalidRequest(ErrorCode errorCode) {
        clearEntryPoint();
        nextSequence();
        MDC.put(EVENT_TYPE.key(), INVALID_REQUEST.value());
        MDC.put(ERROR_CODE.key(), errorCode.code());
        MDC.put(ERROR_TEXT.key(), errorCode.description());
    }

    public void generateFault() {
        clearError();
        nextSequence();
        MDC.put(EVENT_TYPE.key(), GENERATE_FAULT.value());
        log.info("Fault response built");
    }

    public void clearError() {
        removeFromMDC(ERROR_CODE.key());
        removeFromMDC(ERROR_TEXT.key());
    }

    public void apiCall() {
        clearEntryPoint();
        nextSequence();
        MDC.put(EVENT_TYPE.key(), API_CALL.value());
        logAPICall();
    }

    public void apiCallError(ErrorCode errorCode) {
        clearEntryPoint();
        nextSequence();
        MDC.put(EVENT_TYPE.key(), API_CALL_ERROR.value());
        MDC.put(ERROR_CODE.key(), errorCode.code());
        MDC.put(ERROR_TEXT.key(), errorCode.description());
    }

    public void persistRecord() {
        nextSequence();
        MDC.put(EVENT_TYPE.key(), PERSIST_RECORD.value());
    }

    public void exitPoint(String endpoint) {
        clearError();
        nextSequence();
        MDC.put(EVENT_TYPE.key(), EXIT_POINT.value());
        logExitPoint(endpoint);
    }

    public void clearAllEntries() {
        removeFromMDC(EVENT_ACTION.key());
        removeFromMDC(UPSTREAM_SERVICE_NAME.key());
        removeFromMDC(REQUEST_ID.key());
        removeFromMDC(EVENT_SEQUENCE.key());
        removeFromMDC(EVENT_TYPE.key());
        removeFromMDC(REQUEST.key());
        removeFromMDC(ERROR_CODE.key());
        removeFromMDC(ERROR_TEXT.key());
    }

    private void removeFromMDC(String logKey) {
        if (MDC.get(logKey) != null) {
            MDC.remove(logKey);
        }
    }

    private void addLogFieldToMDC(String logKey) {
        String logValue = extractLogFieldValue(logKey);
        if (logValue != null) {
            MDC.put(logKey, logValue);
        }
    }

    private String extractLogFieldValue(String logEvent) {
        Map<String, String> logKey = logEventProperties.getLogEvent().get(request.getRequestURI());
        if (logKey != null) {
            return logKey.get(logEvent);
        }
        return null;
    }

    private void logEntryPoint() {
        log.info("Received request for {}", extractLogFieldValue("entry.message"));
    }

    private void logAPICall() {
        log.info("Sending request for {} to EIS", extractLogFieldValue("entry.message"));
    }

    public void logAPICallResponse(Object response) {
        MDC.put(RESPONSE.key(), XmlMarshaller.marshal(response));
        log.info("Received response for {} from EIS", extractLogFieldValue("entry.message"));
        removeFromMDC(RESPONSE.key());
    }

    private void logExitPoint(String endpoint) {
        if (logEventProperties.getLogEvent().get(endpoint) != null) {
            log.info("Sending response");
        }
    }

}
