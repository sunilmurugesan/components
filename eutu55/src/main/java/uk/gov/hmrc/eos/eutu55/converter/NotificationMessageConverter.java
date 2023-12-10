package uk.gov.hmrc.eos.eutu55.converter;

public class NotificationMessageConverter extends SchemaMarshallingMessageConverter {

    private static final String TARGET_NAMESPACE = "http://xmlns.ec.eu/BusinessActivityService/IOSS_DR/INotificationCBS/V1";
    private static final String XSD = "xsd/notification/NotificationCBS.xsd";
    private static final String CONTEXT_PATH = "uk.gov.hmrc.eu.eutu55.notification";

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
