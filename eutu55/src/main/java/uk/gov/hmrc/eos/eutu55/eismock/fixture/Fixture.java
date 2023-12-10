package uk.gov.hmrc.eos.eutu55.eismock.fixture;

import org.springframework.util.ResourceUtils;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

public class Fixture {

    public static String content(String filename) {
        try {
            try (InputStream stream = ResourceUtils.getURL(String.format("classpath:%s", filename)).openStream()) {
                return StreamUtils.copyToString(stream, Charset.defaultCharset());
            }
        } catch (IOException e) {
            System.err.println("Error occurred!");
            e.printStackTrace();
        }
        return "";
    }

}
