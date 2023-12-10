package uk.gov.hmrc.eos.eutu55.bdd.stepdefinition;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import uk.gov.hmrc.eos.eutu55.model.StatusResultResponse;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Scanner;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Log4j2
public class StepDefinition {

    private final WireMockServer wireMockServer = new WireMockServer(options().dynamicPort());
    private final CloseableHttpClient httpClient = HttpClients.createDefault();
    private HttpResponse httpResponse;

    @Given("Status service is running successfully")
    public void statusServiceRunning(){
        log.info("Status service running successfully.......");
    }

    @When("Request received for status")
    public void test() throws IOException {
        wireMockServer.start();

        StatusResultResponse statusResponse = StatusResultResponse.builder()
                .status(1)
                .totalItems(100L)
                .lastDisseminationDateTime(LocalDateTime.of(2022, 6, 9, 0, 0, 0)).build();

        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        final String statusResponseJson = mapper.writeValueAsString(statusResponse);

        configureFor("localhost", wireMockServer.port());
        stubFor(get(urlEqualTo("/status/v1")).withHeader("accept", equalTo(APPLICATION_JSON_VALUE))
                .willReturn(aResponse().withBody(statusResponseJson)));

        HttpGet request = new HttpGet("http://localhost:" + wireMockServer.port() + "/status/v1");
        request.addHeader("accept", APPLICATION_JSON_VALUE);
        httpResponse = httpClient.execute(request);

        assertThat(statusResponseJson).isEqualTo(convertResponseToString(httpResponse));
        verify(getRequestedFor(urlEqualTo("/status/v1")).withHeader("accept", equalTo(APPLICATION_JSON_VALUE)));

        wireMockServer.stop();
    }

    @Then("Status fetch is successful")
    public void successResponseCode(){
        assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(200);
    }

    private String convertResponseToString(HttpResponse response) throws IOException {
        InputStream responseStream = response.getEntity().getContent();
        Scanner scanner = new Scanner(responseStream, StandardCharsets.UTF_8);
        String responseString = scanner.useDelimiter("\\Z").next();
        scanner.close();
        return responseString;
    }
}
