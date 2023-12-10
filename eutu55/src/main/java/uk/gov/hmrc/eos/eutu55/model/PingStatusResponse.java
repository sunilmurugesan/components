package uk.gov.hmrc.eos.eutu55.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.hmrc.eos.eutu55.utils.Status;

@AllArgsConstructor
@Getter
public class PingStatusResponse {
    private Status status;
}
