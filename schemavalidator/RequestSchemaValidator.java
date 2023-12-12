package uk.gov.hmrc.pds.ards.schemavalidator;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonInputMessage;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class RequestSchemaValidator extends RequestBodyAdviceAdapter {

  private final ResourceLoader resourceLoader;

  /**
   * This method is invoked by the framework to determine whether this interceptor should be invoked
   * or not.
   */
  @Override
  public boolean supports(
      MethodParameter methodParameter,
      Type targetType,
      Class<? extends HttpMessageConverter<?>> converterType) {

    String schema = extractSchema(targetType);
    Resource resource = resourceLoader.getResource(String.format("classpath:%s", schema));
    if (!resource.exists()) {
      String errorMessage = "Schema mapped to the request cannot be found.";
      log.error("{}", errorMessage);
      throw new SchemaNotFoundException(errorMessage);
    }
    return resource.exists();
  }

  /**
   * This method allows validation of request body against the json schema before it gets converted
   * into the target object, and before it is passed into a controller method as @RequestBody
   */
  @Override
  public HttpInputMessage beforeBodyRead(
      HttpInputMessage inputMessage,
      MethodParameter parameter,
      Type targetType,
      Class<? extends HttpMessageConverter<?>> converterType)
      throws IOException {

    InputStream validatedMessageStream;

    try (FastByteArrayOutputStream outputStream = new FastByteArrayOutputStream();
        InputStream inputMessageStream = inputMessage.getBody()) {
      inputMessageStream.transferTo(outputStream);

      this.validate(extractSchema(targetType), outputStream.getInputStream());

      validatedMessageStream = outputStream.getInputStream();
    }
    return new MappingJacksonInputMessage(validatedMessageStream, inputMessage.getHeaders());
  }

  /** This extracts the json schema name from the request payload object passed as @RequestBody */
  private String extractSchema(Type targetType) {
    return "schemas/" + StringUtils.uncapitalize(((Class<?>) targetType).getSimpleName()) + ".json";
  }

  /** Validates the request body against the corresponding json schema. */
  private void validate(String schema, InputStream payloadStream) throws IOException {
    Resource resource = resourceLoader.getResource(String.format("classpath:%s", schema));
    if (resource.exists()) {
      log.info("Validating request payload against the schema {}", resource.getFilename());
      JsonSchemaValidator.validateRequest(resource.getInputStream(), payloadStream);
    }
  }
}
