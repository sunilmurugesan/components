package uk.gov.hmrc.eos.eutu55.logger;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.AbstractRequestLoggingFilter;
import uk.gov.hmrc.eos.eutu55.config.RequestCorrelationId;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

import static uk.gov.hmrc.eos.eutu55.config.RequestCorrelationId.X_CORRELATION_ID;


@Component
@Slf4j
public final class RequestLoggingFilter extends AbstractRequestLoggingFilter {

    @Autowired
    private LoggerComponent loggerComponent;

    public RequestLoggingFilter() {
        this.setBeforeMessagePrefix("Calling endpoint [");
    }

    @Override
    protected void beforeRequest(HttpServletRequest request, String message) {
        log.info(message);
        RequestCorrelationId.setRequestId(requestId(request));
    }

    private String requestId(HttpServletRequest request) {
        if (request.getHeader(X_CORRELATION_ID) == null) {
            return UUID.randomUUID().toString();
        }
        return request.getHeader(X_CORRELATION_ID);
    }

    @Override
    protected void afterRequest(HttpServletRequest request, String message) {
        loggerComponent.exitPoint(request.getRequestURI());
        loggerComponent.clearAllEntries();
    }
}
