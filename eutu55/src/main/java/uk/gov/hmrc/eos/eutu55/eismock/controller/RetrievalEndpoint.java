package uk.gov.hmrc.eos.eutu55.eismock.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmrc.eos.eutu55.eismock.fixture.Fixture;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/pds/cnit/eutu55/drs/iossdr")
public class RetrievalEndpoint {

    @PostMapping(value = "/retrieval/v1", produces = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> retrieve(@RequestBody String body) {
        return ResponseEntity.ok(Fixture.content("eismock/retrieval/accept.xml"));
    }
}
