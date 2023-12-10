package uk.gov.hmrc.eutu55.request;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RequestGeneratorRequest {
    private Integer createCount;
    private Integer updateCount;
    private Integer deleteCount;
    private Integer sequence;
}
