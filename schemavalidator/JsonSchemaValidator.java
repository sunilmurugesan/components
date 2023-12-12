package uk.gov.hmrc.pds.ards.schemavalidator;

import static uk.gov.hmrc.pds.ards.exception.BusinessError.REQUEST_VALIDATION_FAILED;
import static uk.gov.hmrc.pds.ards.exception.BusinessError.RESPONSE_VALIDATION_FAILED;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmrc.pds.ards.exception.ARSSchemaFailureException;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonSchemaValidator {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  static {
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    objectMapper.registerModule(new JavaTimeModule());
  }

  public static void validateRequest(InputStream schemaStream, InputStream payloadStream)
      throws IOException {
    List<String> validationErrors = validate(schemaStream, payloadStream);
    if (!validationErrors.isEmpty()) {
      throw new ARSSchemaFailureException(REQUEST_VALIDATION_FAILED, validationErrors);
    }
  }

  public static void validateResponse(InputStream schemaStream, Object payload) throws IOException {
    InputStream payloadStream =
        new BufferedInputStream(new ByteArrayInputStream(objectMapper.writeValueAsBytes(payload)));
    List<String> validationErrors = validate(schemaStream, payloadStream);
    if (!validationErrors.isEmpty()) {
      throw new ARSSchemaFailureException(RESPONSE_VALIDATION_FAILED, validationErrors);
    }
  }

  /** Validates the json payload against the corresponding json schema. */
  private static List<String> validate(InputStream schemaStream, InputStream payloadStream)
      throws IOException {
    try (schemaStream;
        payloadStream) {
      JsonSchema jsonSchema =
          JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7).getSchema(schemaStream);
      JsonNode jsonNode = objectMapper.readTree(payloadStream);
      Set<ValidationMessage> validationMessages = jsonSchema.validate(jsonNode);
      return collectValidationErrors(validationMessages);
    }
  }

  private static List<String> collectValidationErrors(Set<ValidationMessage> validationMessages) {
    return validationMessages.stream()
        .map(ValidationMessage::getMessage)
        .collect(Collectors.toList());
  }
}
