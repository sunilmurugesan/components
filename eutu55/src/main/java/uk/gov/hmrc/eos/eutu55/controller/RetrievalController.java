package uk.gov.hmrc.eos.eutu55.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmrc.eos.eutu55.model.RetrievalRequest;
import uk.gov.hmrc.eos.eutu55.model.RetrievalResultResponse;
import uk.gov.hmrc.eos.eutu55.service.RetrievalService;

import javax.validation.Valid;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/pds/cnit/eutu55")
public class RetrievalController {

    private final RetrievalService retrievalService;

    @PostMapping(value = "/retrieval/v1")
    ResponseEntity<RetrievalResultResponse> retrieveData(@RequestHeader(name="userId") String userId,
                                                         @Valid @RequestBody RetrievalRequest request){
        return ResponseEntity.ok(retrievalService.processRetrieval(request, userId));
    }
}
