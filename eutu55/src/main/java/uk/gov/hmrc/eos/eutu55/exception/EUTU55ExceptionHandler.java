package uk.gov.hmrc.eos.eutu55.exception;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.hmrc.eos.eutu55.logger.LoggerComponent;
import uk.gov.hmrc.eos.eutu55.model.Error;
import uk.gov.hmrc.eos.eutu55.utils.AlertUtils;
import uk.gov.hmrc.eos.eutu55.utils.ErrorCode;

import java.time.LocalDateTime;

import static uk.gov.hmrc.eos.eutu55.utils.ErrorCode.INVALID_REQUEST;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class EUTU55ExceptionHandler {

    private final LoggerComponent loggerComponent;

    @ExceptionHandler(value = {EISGatewayException.class})
    protected ResponseEntity<Error> handleEISException(EISGatewayException ex){
        loggerComponent.apiCallError(ex.getErrorCode());
        log.error("Error! {}", ex.getErrorCode().description());
        Error error = generateError(ex.getErrorCode());
        log.error(AlertUtils.builder().errorCode(error.getCode())
                .errorMessage(error.getDescription())
                .timeStamp(LocalDateTime.now())
                .journey("EIS Integration")
                .build().toString());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({
            HttpMessageConversionException.class,
            MethodArgumentNotValidException.class
    })
    protected ResponseEntity<Error> handleInvalidRequestException(Exception ex){
        loggerComponent.invalidRequest(INVALID_REQUEST);
        log.error("Error! {}", INVALID_REQUEST.description() + ". " + ex.getMessage());
        Error error = generateError(INVALID_REQUEST);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(error, headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = {EUTU55Exception.class})
    protected ResponseEntity<Error> handleEUTU55Exception(EUTU55Exception ex){
        loggerComponent.apiCallError(ex.getErrorCode());
        log.error("Error! {}", ex.getErrorCode().description());
        Error error = generateError(ex.getErrorCode());
        log.error(AlertUtils.builder().errorCode(error.getCode())
                .errorMessage(error.getDescription())
                .timeStamp(LocalDateTime.now())
                .journey("EIS Integration")
                .build().toString());
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(error, headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Error generateError(ErrorCode errorCode) {
        loggerComponent.generateFault();
        return new Error(errorCode.code(), errorCode.description());
    }

}
