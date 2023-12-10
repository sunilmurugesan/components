package uk.gov.hmrc.eos.eutu55.config;


import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.hmrc.eos.eutu55.converter.FaultMessageConverter;
import uk.gov.hmrc.eos.eutu55.converter.NotificationMessageConverter;
import uk.gov.hmrc.eos.eutu55.converter.SynchronisationMessageConverter;

@EnableWebMvc
@Configuration
public class Eutu55WebConfigurer implements WebMvcConfigurer {

  @Value("${cors.allowed.origins:}")
  private String[] allowedOrigins;

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> messageConverters) {
    messageConverters.add(new SynchronisationMessageConverter());
    messageConverters.add(new NotificationMessageConverter());
    messageConverters.add(new StringHttpMessageConverter());
    messageConverters.add(new FaultMessageConverter());
    messageConverters.add(new MappingJackson2HttpMessageConverter());
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    String[] origins = allowedOrigins;
    if (allowedOrigins.length == 0) {
      origins = new String[]{"*"};
    }
    registry.addMapping("/**").allowedOrigins(origins);
  }

}