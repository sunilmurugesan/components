package uk.gov.hmrc.eos.eutu55.converter;

import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.converter.xml.MarshallingHttpMessageConverter;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.xml.bind.Marshaller;
import java.util.Map;

import static java.util.Map.entry;

public abstract class SchemaMarshallingMessageConverter extends MarshallingHttpMessageConverter {

    @SneakyThrows
    public SchemaMarshallingMessageConverter() {
        Jaxb2Marshaller marshaller = marshaller();
        this.setMarshaller(marshaller);
        this.setUnmarshaller(marshaller);
    }

    private Jaxb2Marshaller marshaller() throws Exception {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPaths(contextPath());

        Resource xsd = new ClassPathResource(xsd());
        marshaller.setSchemas(xsd);

        marshaller.setMarshallerProperties(Map.ofEntries(
                entry(Marshaller.JAXB_SCHEMA_LOCATION, targetNamespace() + " " + xsd.getURL()),
                entry(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE)));

        marshaller.afterPropertiesSet();
        return marshaller;
    }

    abstract String targetNamespace();

    abstract String xsd();

    abstract String contextPath();

}
