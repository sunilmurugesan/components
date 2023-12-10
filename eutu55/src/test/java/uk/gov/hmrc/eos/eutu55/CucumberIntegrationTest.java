package uk.gov.hmrc.eos.eutu55;

import io.cucumber.junit.CucumberOptions;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@CucumberOptions(features = "classpath:bdd/features", plugin = "json:target/cucumber.json")
@CucumberContextConfiguration
public class CucumberIntegrationTest {
}
