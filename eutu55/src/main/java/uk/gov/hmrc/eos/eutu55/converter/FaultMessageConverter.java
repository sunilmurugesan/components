package uk.gov.hmrc.eos.eutu55.converter;

import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import uk.gov.hmrc.eos.eutu55.model.Fault;

import javax.xml.bind.Marshaller;
import java.util.Map;

public class FaultMessageConverter extends MarshallingHttpMessageConverter {

    public FaultMessageConverter() {
        Jaxb2Marshaller marshaller = marshaller();
        this.setMarshaller(marshaller);
        this.setUnmarshaller(marshaller);
    }

    private Jaxb2Marshaller marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(Fault.class);
        marshaller.setMarshallerProperties(Map.of(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE));
        return marshaller;
    }
}
