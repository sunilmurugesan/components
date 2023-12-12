package uk.gov.hmrc.eos.util.excel;


import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies the column name where the corresponding value is mapped from the Excel data
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface ExcelColumn {
    /**
     *
     * @return Excel column name
     */
    String value();
}