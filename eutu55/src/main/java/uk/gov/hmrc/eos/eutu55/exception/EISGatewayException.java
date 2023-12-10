package uk.gov.hmrc.eos.eutu55.exception;

import lombok.Getter;
import uk.gov.hmrc.eos.eutu55.utils.ErrorCode;

@Getter
public class EISGatewayException extends RuntimeException {

    private ErrorCode errorCode;

    public EISGatewayException(ErrorCode errorCode) {
        super(errorCode.description());
        this.errorCode = errorCode;
    }
    
    public EISGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}
