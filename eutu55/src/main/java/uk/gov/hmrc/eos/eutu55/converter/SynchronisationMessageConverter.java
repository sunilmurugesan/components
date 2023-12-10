package uk.gov.hmrc.eos.eutu55.converter;

public class SynchronisationMessageConverter extends SchemaMarshallingMessageConverter {

    private static final String TARGET_NAMESPACE = "http://xmlns.ec.eu/BusinessActivityService/IOSS_DR/ISynchronisationCBS/V1";
    private static final String XSD = "xsd/synchronisation/SynchronisationCBS.xsd";
    private static final String CONTEXT_PATH = "uk.gov.hmrc.eu.eutu55.synchronisation";

    @Override
    String targetNamespace() {
        return TARGET_NAMESPACE;
    }

    @Override
    String xsd() {
        return XSD;
    }

    @Override
    String contextPath() {
        return CONTEXT_PATH;
    }
}
