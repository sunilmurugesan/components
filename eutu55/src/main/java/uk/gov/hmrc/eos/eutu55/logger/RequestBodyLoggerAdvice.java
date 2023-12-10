package uk.gov.hmrc.eos.eutu55.logger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJacksonInputMessage;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;
import uk.gov.hmrc.eos.eutu55.converter.SynchronisationMessageConverter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.stream.Collectors;

import static uk.gov.hmrc.eos.eutu55.logger.LogField.REQUEST;

@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class RequestBodyLoggerAdvice extends RequestBodyAdviceAdapter {

    private final LoggerComponent loggerComponent;

    @Value("${log.dissemination.show-request-payload: false}")
    private boolean showDisseminationRequestPayload;

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return !(converterType == StringHttpMessageConverter.class);
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter,
                                           Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {

        InputStream inputMessageStreamCopy;

        try (FastByteArrayOutputStream outputStream = new FastByteArrayOutputStream();
             InputStream inputMessageStream = inputMessage.getBody()) {
            inputMessageStream.transferTo(outputStream);

            String result = new BufferedReader(new InputStreamReader(outputStream.getInputStream()))
                    .lines().collect(Collectors.joining());

            if(!(converterType == SynchronisationMessageConverter.class)) {
                MDC.put(REQUEST.key(), result);
            }

            if(showDisseminationRequestPayload && converterType == SynchronisationMessageConverter.class) {
                MDC.put(REQUEST.key(), result);
            }

            loggerComponent.entryPoint();

            inputMessageStreamCopy = outputStream.getInputStream();
        }
        return new MappingJacksonInputMessage(inputMessageStreamCopy, inputMessage.getHeaders());
    }

}
