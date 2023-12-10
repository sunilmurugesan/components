package uk.gov.hmrc.eos.eutu55.ui;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UILoggingService {
	
	public static final String RESPONSE_MESSAGE = "The message has been logged";
	
	public UILoggingResponse getResponse(UILoggingRequest request) {
		log.info("ui log is " + request.getLogmessage());
		return new UILoggingResponse(RESPONSE_MESSAGE);
	}

}
