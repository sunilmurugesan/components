package uk.gov.hmrc.eos.eutu55.helper;

import lombok.SneakyThrows;
import uk.gov.hmrc.eos.eutu55.utils.XmlMarshaller;

import java.nio.file.Files;
import java.nio.file.Paths;

public class TestHelper {

    public static String request(String filename) {
        String payloadFilePath = "src/test/resources/payload/request/";
        return fileContent(payloadFilePath + filename);
    }

    public static String response(String filename) {
        String responseFilePath = "src/test/resources/payload/response/";
        return fileContent(responseFilePath + filename);
    }

    @SneakyThrows
    private static String fileContent(String file) {
        return new String(Files.readAllBytes(Paths.get(file)));
    }

}
