package uk.gov.hmrc.eos.eutu55.model;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType(propOrder = {
        "statusCode",
        "faultMessage"
})
@Data
public class Fault {
    private String statusCode;
    private String faultMessage;

    public static Fault newInstance(String statusCode, String faultMessage) {
        Fault fault = new Fault();
        fault.setStatusCode(statusCode);
        fault.setFaultMessage(faultMessage);
        return fault;
    }
}