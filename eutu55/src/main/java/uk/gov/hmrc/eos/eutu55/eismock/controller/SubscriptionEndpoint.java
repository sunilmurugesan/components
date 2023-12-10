package uk.gov.hmrc.eos.eutu55.eismock.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmrc.eos.eutu55.eismock.fixture.Fixture;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/pds/cnit/eutu55/drs/iossdr")
public class SubscriptionEndpoint {

    private static final Pattern PATTERN = Pattern.compile("contactEmail>([\\S\\s]+)</", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

    @PostMapping(value = "/updatesubscription/v1", produces = MediaType.APPLICATION_XML_VALUE)
    public String subscribe(@RequestBody String body) {
        Matcher matcher = PATTERN.matcher(body);
        if (matcher.find()) {
            String email = matcher.group(1).toLowerCase();
            if (!email.contains("reject")) {
                return Fixture.content("eismock/subscription/accept.xml");
            }
        }
        return Fixture.content("eismock/subscription/reject.xml");
    }
}
