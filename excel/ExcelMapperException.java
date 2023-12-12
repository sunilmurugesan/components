package uk.gov.hmrc.eos.util.excel;

public class ExcelMapperException extends RuntimeException {

    public ExcelMapperException(String message) {
        super(message);
    }

    public ExcelMapperException(String message, Throwable cause) {
        super(message, cause);
    }

}
