package uk.gov.hmrc.eos.eutu55.eismock.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmrc.eos.eutu55.eismock.fixture.Fixture;

@RestController
@RequestMapping("/pds/cnit/eutu55/drs/iossdr")
public class StatusEndpoint {

    @GetMapping(value = "/status/v1", produces = MediaType.APPLICATION_XML_VALUE)
    public String retrieve() {
        return Fixture.content("eismock/status/accept.xml");
    }
}
