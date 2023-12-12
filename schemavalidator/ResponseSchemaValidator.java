package uk.gov.hmrc.pds.ards.schemavalidator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Set;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "schema.validation.response")
public class ResponseSchemaValidator implements ResponseBodyAdvice<Object> {

  private static final Set<String> unsupportedReturnTypes =
      Set.of("String", "boolean", "Map", "void");

  private final ResourceLoader resourceLoader;

  /**
   * This method is invoked by the framework to determine whether this interceptor should be invoked
   * or not. This is not invoked if the controller return type 'String', 'boolean', 'Map' or 'void'
   * If the schema for the return type cannot be determined or found then it returns a
   * 'SchemaNotFoundException'.
   */
  @Override
  public boolean supports(MethodParameter returnType, Class converterType) {

    if (isUnSupportedReturnType(returnType.getGenericParameterType())) {
      return false;
    }

    String schemaName = extractSchemaFromReturnType(returnType);
    Resource resource = resourceLoader.getResource(String.format("classpath:%s", schemaName));
    if (!resource.exists()) {
      String errorMessage = String.format("Schema %s not found.", schemaName);
      log.error("Schema {} mapped to the response cannot be found.", schemaName);
      throw new SchemaNotFoundException(errorMessage);
    }
    return resource.exists();
  }

  /**
   * This method allows validation of response body against the json schema before it gets
   * serialized to json response body. The 'returnType' here is the controller return type.
   */
  @Override
  public Object beforeBodyWrite(
      Object body,
      MethodParameter returnType,
      MediaType selectedContentType,
      Class selectedConverterType,
      ServerHttpRequest request,
      ServerHttpResponse response) {

    String schemaName = extractSchemaFromReturnType(returnType);
    validate(schemaName, body);
    return body;
  }

  public void validate(String schema, Object body) {
    try {
      Resource resource = resourceLoader.getResource(String.format("classpath:%s", schema));
      JsonSchemaValidator.validateResponse(resource.getInputStream(), body);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  private boolean isUnSupportedReturnType(Type parameterType) {
    Type type = extractType(parameterType);
    return type instanceof ParameterizedType
        || unsupportedReturnTypes.contains(((Class<?>) type).getSimpleName());
  }

  private String extractSchemaFromReturnType(MethodParameter methodParameter) {

    String schemaName = extractSchemaNameIfSupplied(methodParameter);
    if (schemaName != null) {
      return "schemas/" + schemaName;
    }
    Type type = methodParameter.getGenericParameterType();
    if (type instanceof ParameterizedType) {
      type = extractType(type);
    }
    return "schemas/" + StringUtils.uncapitalize(((Class<?>) type).getSimpleName()) + ".json";
  }

  private String extractSchemaNameIfSupplied(MethodParameter methodParameter) {
    String schemaName = null;
    if (methodParameter.getMethodAnnotations() != null) {
      schemaName =
          Arrays.stream(methodParameter.getMethodAnnotations())
              .filter(annotation -> annotation.annotationType() == Schema.class)
              .map(annotation -> ((Schema) annotation).value())
              .findFirst()
              .orElse(null);
    }
    return schemaName;
  }

  private Type extractType(Type type) {
    return type instanceof ParameterizedType
        ? ((ParameterizedType) type).getActualTypeArguments()[0]
        : type;
  }
}
