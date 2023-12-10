package uk.gov.hmrc.eos.eutu55.utils;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertUtils {
  private String errorCode;
  private String errorMessage;
  private LocalDateTime timeStamp;
  private String journey;

  @Override
  public String toString() {
    return "{" +
        "\"ALERT\": {" +
        "\"error_code\": \""+errorCode+"\"," +
        "\"error_message\": \""+errorMessage+"\"," +
        "\"timestamp\": \""+timeStamp+"\"," +
        "\"journey\": \""+journey+"\"" +
        "}}";
  }
}