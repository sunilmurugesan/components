package uk.gov.hmrc.pds.ards.schemavalidator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * To explicitly specify the name of the schema in the controller, If provided, the schema validator
 * uses this schema to validate the json payload.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Schema {

  /**
   * @return Schema name
   */
  String value();
}
