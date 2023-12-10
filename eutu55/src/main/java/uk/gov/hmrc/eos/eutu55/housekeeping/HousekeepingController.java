package uk.gov.hmrc.eos.eutu55.housekeeping;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pds/cnit/eutu55")
public class HousekeepingController {

    private final HousekeepingService housekeepingService;

    @PostMapping(value = "/housekeeping/v1")
    void performHousekeeping(@RequestBody Optional<HousekeepingRequest> request) {
        Integer retentionPeriod = request.map(HousekeepingRequest::getRetentionPeriod).orElse(null);
        housekeepingService.performHousekeeping(retentionPeriod);
    }
}
