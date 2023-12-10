package uk.gov.hmrc.eos.eutu55.exception;

import lombok.Getter;
import uk.gov.hmrc.eos.eutu55.utils.ErrorCode;

@Getter
public class EUTU55Exception extends RuntimeException {

    private final ErrorCode errorCode;

    public EUTU55Exception(ErrorCode errorCode) {
        super(errorCode.description());
        this.errorCode = errorCode;
    }
}
