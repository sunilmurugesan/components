package uk.gov.hmrc.eos.eutu55.eismock.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmrc.eos.eutu55.eismock.fixture.Fixture;

@RestController
@RequestMapping("/pds/cnit/eutu55/drs/iossdr")
public class PingEndpoint {

    @GetMapping(value = "/ping/v1", produces = MediaType.APPLICATION_XML_VALUE)
    public String ping() {
        return Fixture.content("eismock/ping/up.xml");
    }
}
