package uk.gov.hmrc.eos.eutu55.ui;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@RestController
@RequestMapping("/pds/cnit/eutu55/ui")
public class UILoggingController {
	
private final UILoggingService uiLoggingService;
	
	@PostMapping(value = "/log")
	public ResponseEntity<UILoggingResponse> createLog(@RequestBody UILoggingRequest request) {
		UILoggingResponse uiLoggingResponse = uiLoggingService.getResponse(request);
		return ResponseEntity.ok(uiLoggingResponse);
	}

}
