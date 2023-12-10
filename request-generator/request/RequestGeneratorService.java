package uk.gov.hmrc.eutu55.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.stream.IntStream;

@Service
@Slf4j
public class RequestGeneratorService {

    private static final String SERVICES_TYPE_NS = "http://xmlns.ec.eu/BusinessObjects/IOSS_DR/Common/V1";

    private final TransformerFactory transformerFactory;
    private final DocumentBuilderFactory documentBuilderFactory;


    public RequestGeneratorService() {
        transformerFactory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
    }

    public String generate(final RequestGeneratorRequest request) {
        int count = defaultIfNull(request.getCreateCount()) + defaultIfNull(request.getUpdateCount()) + defaultIfNull(request.getDeleteCount());
        int sequence = defaultIfNull(request.getSequence()) > 0 ? defaultIfNull(request.getSequence()) - 1 : defaultIfNull(request.getSequence());
        String createXml = generateCreate(count, sequence);

        if (defaultIfNull(request.getUpdateCount()) == 0 && defaultIfNull(request.getDeleteCount()) == 0) {
            return createXml;
        }

        Document document = document(createXml);
        NodeList nodeList = document.getElementsByTagNameNS(SERVICES_TYPE_NS, "iossVatNumberUpdate");
        generateUpdate(nodeList, defaultIfNull(request.getUpdateCount()), count);
        generateDelete(nodeList, defaultIfNull(request.getDeleteCount()), count);

        return generateXml(document);
    }

    private String generateXml(Document document) {
        try {
            Transformer transformer = transformerFactory.newTransformer();
            // pretty print
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "no");

            DOMSource source = new DOMSource(document);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);
            return writer.toString();
        } catch (Exception e) {
            log.error("Exception occurred!", e);
        }
        return "</>";
    }

    private String generateCreate(int count, int sequence) {
        try {
            try (InputStream xslStream = ResourceUtils.getURL("classpath:request/request-generator.xslt").openStream();
                 InputStream templateStream = ResourceUtils.getURL("classpath:request/template.xml").openStream()) {
                Source xsl = new StreamSource(xslStream);
                Source template = new StreamSource(templateStream);
                Transformer transformer = transformerFactory.newTransformer(xsl);
                transformer.setParameter("count", count);
                transformer.setParameter("sequence", sequence);
                StringWriter writer = new StringWriter();
                StreamResult result = new StreamResult(writer);
                transformer.transform(template, result);
                return writer.toString();
            }
        } catch (Exception e) {
            log.error("Exception occurred!", e);
        }
        return "</>";
    }

    private void generateUpdate(NodeList nodes, int count, int max) {
        RandomNumberGenerator generator = new RandomNumberGenerator(count, max);
        long total = IntStream.rangeClosed(1, count)
                .mapToObj(i -> getNodeToAmend(nodes, generator.random()).getChildNodes())
                .flatMap(children -> IntStream.range(0, children.getLength())
                        .mapToObj(index -> children.item(index))
                        .filter(node -> node.getNodeType() == Node.ELEMENT_NODE)
                        .map(node -> {
                            if ("servicestype:operation".equalsIgnoreCase(node.getNodeName())) {
                                node.setTextContent("U");
                            }
                            if ("servicestype:validityStartDate".equalsIgnoreCase(node.getNodeName())) {
                                node.setTextContent(DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now()));
                            }
                            if ("servicestype:validityEndDate".equalsIgnoreCase(node.getNodeName())) {
                                node.setTextContent(DateTimeFormatter.ofPattern("yyyy-MM-dd").format(LocalDate.now().plusDays(7)));
                            }
                            if ("servicestype:modificationDateTime".equalsIgnoreCase(node.getNodeName())) {
                                node.setTextContent(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.0").format(LocalDateTime.now()));
                            }
                            return node;
                        })).count();
        log.info("Processed {} records for update operation", total / 4);
    }

    private void generateDelete(NodeList nodes, int count, int max) {
        RandomNumberGenerator generator = new RandomNumberGenerator(count, max);
        long total = IntStream.rangeClosed(1, count)
                .mapToObj(i -> getNodeToAmend(nodes, generator.random()).getChildNodes())
                .flatMap(children -> IntStream.range(0, children.getLength())
                        .mapToObj(index -> children.item(index))
                        .filter(Objects::nonNull)
                        .filter(node -> node.getNodeType() == Node.ELEMENT_NODE)
                        .map(node -> {
                            if ("servicestype:operation".equalsIgnoreCase(node.getNodeName())) {
                                node.setTextContent("D");
                            }
                            if ("servicestype:validityStartDate".equalsIgnoreCase(node.getNodeName())) {
                                node.getParentNode().removeChild(node);
                            }
                            if ("servicestype:validityEndDate".equalsIgnoreCase(node.getNodeName())) {
                                node.getParentNode().removeChild(node);
                            }
                            if ("servicestype:modificationDateTime".equalsIgnoreCase(node.getNodeName())) {
                                node.getParentNode().removeChild(node);
                            }
                            return node;
                        })).count();
        log.info("Processed {} records for delete operation", total / 4);
    }

    private Node getNodeToAmend(NodeList nodeList, int index) {
        Node node = nodeList.item(index);
        if (this.hasCreate(node)) {
            return node;
        }
        return IntStream.range(0, nodeList.getLength())
                .mapToObj(i -> nodeList.item(i))
                .filter(n -> hasCreate(n))
                .findFirst().get();

    }

    private boolean hasCreate(Node node) {
        return IntStream.range(0, node.getChildNodes().getLength())
                .mapToObj(i -> node.getChildNodes().item(i))
                .filter(iossVatNumberUpdateNode -> node.getNodeType() == Node.ELEMENT_NODE)
                .filter(Objects::nonNull)
                .filter(iossVatNumberUpdateNode -> "servicestype:operation".equalsIgnoreCase(iossVatNumberUpdateNode.getNodeName()))
                .allMatch(iossVatNumberUpdateNode -> "C".equals(iossVatNumberUpdateNode.getTextContent()));
    }


    private int defaultIfNull(Integer count) {
        return count != null ? count : 0;
    }

    private Document document(String createXml) {
        Document document = null;
        try (InputStream is = new ByteArrayInputStream(createXml.getBytes(StandardCharsets.UTF_8))) {
            DocumentBuilder db = documentBuilderFactory.newDocumentBuilder();
            document = db.parse(is);
        } catch (Exception e) {
            log.error("Exception occurred!", e);
        }
        return document;
    }
}
