package uk.gov.hmrc.eos.eutu55.logger;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@PropertySource(value = "classpath:logger/logEventProperties.json", factory = JsonPropertySourceFactory.class)
@ConfigurationProperties
@Getter
@Setter
public class LogEventProperties {
    private Map<String, Map<String, String>> logEvent;
}