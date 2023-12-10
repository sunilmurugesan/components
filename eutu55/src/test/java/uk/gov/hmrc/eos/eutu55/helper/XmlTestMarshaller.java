package uk.gov.hmrc.eos.eutu55.helper;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;

public class XmlTestMarshaller {
    public static <T> T unmarshall(String xmlMessage, Class<T> clazz) {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(clazz);
        return (T) marshaller.unmarshal(new StreamSource(new StringReader(xmlMessage)));
    }
}
