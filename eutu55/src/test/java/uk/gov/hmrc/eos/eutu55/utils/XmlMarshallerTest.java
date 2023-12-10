package uk.gov.hmrc.eos.eutu55.utils;

import org.junit.jupiter.api.Test;
import uk.gov.hmrc.eu.eutu55.ping.PingResponse;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.hmrc.eos.eutu55.utils.Status.UP;

class XmlMarshallerTest {

    @Test
    void toXmlTextTest() {

        String unmarshalString = XmlMarshaller.marshal(pingResponseMessage());
        assertThat(unmarshalString).isNotNull();
        assertThat(unmarshalString).contains(String.valueOf(UP.getValue()));
        assertThat(unmarshalString).contains("PingResponse");
        assertThat(unmarshalString).contains("operationResult");
        assertThat(unmarshalString).contains("isAlive");
    }

    private PingResponse pingResponseMessage() {
        PingResponse response = new PingResponse();
        PingResponse.OperationResult operationResult = new PingResponse.OperationResult();
        operationResult.setIsAlive(UP.getValue());
        response.setOperationResult(operationResult);
        return response;
    }


}
