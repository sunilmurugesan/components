package uk.gov.hmrc.eos.eutu55.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import uk.gov.hmrc.eos.eutu55.exception.EISGatewayException;
import uk.gov.hmrc.eos.eutu55.helper.XmlTestMarshaller;
import uk.gov.hmrc.eu.eutu55.retrieval.RetrievalRequest;
import uk.gov.hmrc.eu.eutu55.retrieval.RetrievalResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static uk.gov.hmrc.eos.eutu55.helper.TestHelper.request;
import static uk.gov.hmrc.eos.eutu55.helper.TestHelper.response;
import static uk.gov.hmrc.eos.eutu55.utils.Outcome.ACCEPTED;

@ExtendWith(SpringExtension.class)
@RestClientTest(GatewayService.class)
class GatewayServiceTest {
    private static final String BEARER_TOKEN = "f916fa9b-0500-3837-a321-4c76f67499e0";
    @Autowired
    private GatewayService service;
    @Autowired
    private MockRestServiceServer server;

    @Test
    @DisplayName("POST call to a remote endpoint can be made successfully")
    void shouldSuccessfullyMakePostCall() {
        String anEndPoint = "/eis/retrieval";
        String aRequestPayloadFile = "retrieval/request-payload.xml";
        String aResponsePayloadFile = "retrieval/success.xml";

        this.server.expect(requestTo(anEndPoint))
                .andExpect(method(POST))
                .andExpect(content().xml(request(aRequestPayloadFile)))
                .andRespond(withSuccess(response(aResponsePayloadFile), APPLICATION_XML));

        RetrievalResponse response = this.service.post(anEndPoint, XmlTestMarshaller.unmarshall(request(aRequestPayloadFile), RetrievalRequest.class),
                RetrievalResponse.class, BEARER_TOKEN);

        assertThat(response.getOperationResult().getOutcome(), equalTo(ACCEPTED.getValue()));
    }

    @Test
    @DisplayName("GET call to a remote endpoint can be made successfully")
    void shouldSuccessfullyMakeGetCall() {
        String anEndPoint = "/some/get/endpoint";
        String aResponsePayloadFile = "retrieval/success.xml";

        this.server.expect(requestTo(anEndPoint))
                .andExpect(method(GET))
                .andRespond(withSuccess(response(aResponsePayloadFile), APPLICATION_XML));

        RetrievalResponse response = this.service.get(anEndPoint, RetrievalResponse.class, BEARER_TOKEN);

        assertThat(response.getOperationResult().getOutcome(), equalTo(ACCEPTED.getValue()));
    }

    @Test
    @DisplayName("Failure to connect to the remote endpoint can be handled properly")
    void remoteEndpointConnectionIssueCanBeHandled() {
        String anEndPoint = "/some/endpoint";
        String aRequestPayloadFile = "retrieval/request-payload.xml";

        this.server.expect(requestTo(anEndPoint))
                .andExpect(method(POST))
                .andExpect(content().xml(request(aRequestPayloadFile)))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThrows(EISGatewayException.class,
                () -> service.post(anEndPoint, XmlTestMarshaller.unmarshall(request(aRequestPayloadFile), RetrievalRequest.class), RetrievalResponse.class, BEARER_TOKEN));
    }

    @Test
    @DisplayName("EIS returning unexpected empty response body can be handled and proper exception thrown")
    void unexpectedEmptyResponseFromEISCallCanBeHandled() {
        String anEndPoint = "/some/endpoint";
        String aRequestPayloadFile = "retrieval/request-payload.xml";

        this.server.expect(requestTo(anEndPoint))
                .andExpect(method(POST))
                .andExpect(content().xml(request(aRequestPayloadFile)))
                .andRespond(withSuccess());

        EISGatewayException exception = assertThrows(EISGatewayException.class,
                () -> service.post(anEndPoint, XmlTestMarshaller.unmarshall(request(aRequestPayloadFile), RetrievalRequest.class), RetrievalResponse.class, BEARER_TOKEN));
        assertThat(exception.getMessage(), equalTo("EIS returned no response body"));
    }
}