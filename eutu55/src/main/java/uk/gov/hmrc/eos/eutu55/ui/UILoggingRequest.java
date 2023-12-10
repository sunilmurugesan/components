package uk.gov.hmrc.eos.eutu55.ui;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@AllArgsConstructor
@Jacksonized
public class UILoggingRequest {
	
    private Object logmessage;
    
    public UILoggingRequest() {
    	logmessage = new UILoggingRequest(logmessage); 
    }

}
