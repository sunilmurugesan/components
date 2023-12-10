package uk.gov.hmrc.eos.eutu55.integration;

import com.github.tomakehurst.wiremock.common.Slf4jNotifier;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public abstract class AbstractIT {

    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().notifier(new Slf4jNotifier(true)))
            .build();

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("wiremock.test.baseurl", wireMockServer::baseUrl);
    }

    @AfterEach
    void resetAll() {
        wireMockServer.resetAll();
    }
}
