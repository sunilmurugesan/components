package uk.gov.hmrc.pds.ards.schemavalidator;

public class SchemaNotFoundException extends RuntimeException {

  public SchemaNotFoundException(String message) {
    super(message);
  }
}
