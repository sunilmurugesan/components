package uk.gov.hmrc.eutu55.request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RequestGeneratorController {

    private final RequestGeneratorService service;

    @Autowired
    public RequestGeneratorController(final RequestGeneratorService service) {
        this.service = service;
    }

    @GetMapping(value = "/synchronization/request/generate", produces = MediaType.APPLICATION_XML_VALUE,
            consumes = MediaType.APPLICATION_XML_VALUE)
    public ResponseEntity<String> generateRequest(@RequestParam(required = false) final Integer createCount,
                                                  @RequestParam(required = false) final Integer updateCount,
                                                  @RequestParam(required = false) final Integer deleteCount,
                                                  @RequestParam(required = false) final Integer sequence) {

        RequestGeneratorRequest request = RequestGeneratorRequest.builder()
                .createCount(createCount).updateCount(updateCount).deleteCount(deleteCount).sequence(sequence).build();
        String result = service.generate(request);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
