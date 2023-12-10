package uk.gov.hmrc.eos.eutu55.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.oxm.MarshallingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.hmrc.eos.eutu55.logger.LoggerComponent;
import uk.gov.hmrc.eos.eutu55.model.Fault;
import uk.gov.hmrc.eos.eutu55.utils.AlertUtils;
import uk.gov.hmrc.eos.eutu55.utils.ErrorCode;

import java.time.LocalDateTime;

import static uk.gov.hmrc.eos.eutu55.utils.ErrorCode.REQUEST_LIMIT_EXCEEDED;
import static uk.gov.hmrc.eos.eutu55.utils.ErrorCode.REQUEST_VALIDATION_FAILED;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class SynchronisationExceptionHandler {

    private final LoggerComponent loggerComponent;

    public static final String LIMIT_EXCEEDED = "limit was exceeded";

    @ExceptionHandler({MarshallingException.class})
    public ResponseEntity<Fault> handleException(MarshallingException ex) {
        String cause = ex.getCause().getCause().getMessage();
        ErrorCode errorCode = errorCode(cause);
        loggerComponent.invalidRequest(errorCode);
        log.error("Error! {}. Reason: {}", errorCode.description(), cause);
        return ResponseEntity.badRequest().body(fault(errorCode));
    }

    private Fault fault(ErrorCode errorCode) {
        log.error(AlertUtils.builder().errorCode(errorCode.code())
                .errorMessage(errorCode.description())
                .timeStamp(LocalDateTime.now())
                .journey("Synchronisation/Dissemination")
                .build().toString());
        loggerComponent.generateFault();
        return Fault.newInstance(errorCode.code(), errorCode.description());
    }

    private ErrorCode errorCode(String cause) {
        ErrorCode errorCode = REQUEST_VALIDATION_FAILED;
        if (cause != null && cause.contains(LIMIT_EXCEEDED)) {
            errorCode = REQUEST_LIMIT_EXCEEDED;
        }
        return errorCode;
    }

}
