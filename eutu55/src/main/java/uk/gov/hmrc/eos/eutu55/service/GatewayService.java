package uk.gov.hmrc.eos.eutu55.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmrc.eos.eutu55.config.RequestCorrelationId;
import uk.gov.hmrc.eos.eutu55.exception.EISGatewayException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Objects;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static uk.gov.hmrc.eos.eutu55.config.RequestCorrelationId.X_CORRELATION_ID;
import static uk.gov.hmrc.eos.eutu55.utils.EUTU55Constants.DATE;
import static uk.gov.hmrc.eos.eutu55.utils.EUTU55Constants.HEADER_DATE_FORMAT;
import static uk.gov.hmrc.eos.eutu55.utils.EUTU55Constants.HEADER_FORWARDED_HOST;
import static uk.gov.hmrc.eos.eutu55.utils.EUTU55Constants.REQUEST_HEADER_AUTHORIZATION;
import static uk.gov.hmrc.eos.eutu55.utils.EUTU55Constants.REQUEST_HEADER_BEARER;
import static uk.gov.hmrc.eos.eutu55.utils.EUTU55Constants.X_FORWARDED_HOST;
import static uk.gov.hmrc.eos.eutu55.utils.ErrorCode.EUTU55_EIS_GATEWAY_EXCEPTION;
import static uk.gov.hmrc.eos.eutu55.utils.ErrorCode.EUTU55_EIS_NO_RESPONSE_BODY;

@Service
@Slf4j
public class GatewayService {

  private final RestTemplate restTemplate;

  public GatewayService(RestTemplateBuilder restTemplateBuilder) {
    this.restTemplate = restTemplateBuilder.build();
  }

  public <T> T post(String url, Object request, Class<T> typedClass, String authorizationBearerToken) {
    return call(POST, url, request, typedClass, authorizationBearerToken);
  }

  public <T> T get(String url, Class<T> typedClass, String authorizationBearerToken) {
    return call(GET, url, null, typedClass, authorizationBearerToken);
  }

  private <T> T call(HttpMethod method, String url, Object request, Class<T> typedClass, String authorizationBearerToken) {
    ResponseEntity<T> response;
    HttpHeaders requestHeaders = httpHeaders(authorizationBearerToken);
    log.info("{} passed to EIS: {}", X_CORRELATION_ID, requestHeaders.get(X_CORRELATION_ID));
    try {
      log.info("Calling EIS endpoint {}", url);
      switch (method) {
        case POST:
          requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_XML));
          response = this.restTemplate.postForEntity(url, new HttpEntity<>(request, requestHeaders), typedClass);
          break;
        case GET:
          response = this.restTemplate.exchange(url, GET, new HttpEntity<>(request, requestHeaders), typedClass);
          break;
        default:
          throw new UnsupportedOperationException("Not implemented!");
      }
      if (Objects.isNull(response.getBody())) {
        log.error("No response body received from the endpoint {}", url);
        throw new EISGatewayException(EUTU55_EIS_NO_RESPONSE_BODY);
      }
      HttpHeaders responseHeaders = response.getHeaders();
      log.info("{} received from EIS: {}", X_CORRELATION_ID, responseHeaders.get(X_CORRELATION_ID));
    } catch (RestClientException rcEx) {
      log.error("Error calling endpoint {}", url, rcEx);
      throw new EISGatewayException(EUTU55_EIS_GATEWAY_EXCEPTION);
    }
    return response.getBody();
  }

  private HttpHeaders httpHeaders(String authorizationBearerToken) {
    final HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_XML);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(HEADER_DATE_FORMAT);
    headers.set(DATE, LocalDateTime.now().format(formatter)
        + " GMT");//need to amend format string to pass GMT and remove hard coded string
    headers.set(X_CORRELATION_ID, RequestCorrelationId.getRequestId());
    headers.set(X_FORWARDED_HOST, HEADER_FORWARDED_HOST);
    headers.set(REQUEST_HEADER_AUTHORIZATION, REQUEST_HEADER_BEARER+authorizationBearerToken);
    return headers;
  }
}
