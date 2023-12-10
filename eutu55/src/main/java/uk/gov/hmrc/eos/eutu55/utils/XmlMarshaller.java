package uk.gov.hmrc.eos.eutu55.utils;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmrc.eu.eutu55.ping.PingResponse;
import uk.gov.hmrc.eu.eutu55.retrieval.RetrievalResponse;
import uk.gov.hmrc.eu.eutu55.status.StatusResponse;
import uk.gov.hmrc.eu.eutu55.subscription.UpdateSubscriptionRespMsg;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;

import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;

@Slf4j
public class XmlMarshaller {
    private static JAXBContext ctx;

    static {
        try {
            ctx = JAXBContext.newInstance(
                    PingResponse.class,
                    UpdateSubscriptionRespMsg.class,
                    RetrievalResponse.class,
                    StatusResponse.class
            );
        } catch (JAXBException e) {
            log.error("Error creating JAXB context", e);
        }
    }

    public static String marshal(Object xmlObj) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Marshaller marshaller = ctx.createMarshaller();
            marshaller.setProperty(JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(xmlObj, baos);
        } catch (Exception e) {
            log.error("Exception while printing XML Object as text", e);
        }
        return baos.toString();
    }

}
