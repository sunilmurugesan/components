package uk.gov.hmrc.eos.eutu55.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import static uk.gov.hmrc.eos.eutu55.config.RequestCorrelationId.X_CORRELATION_ID;
import static uk.gov.hmrc.eos.eutu55.utils.EUTU55Constants.CONTENT_TYPE;
import static uk.gov.hmrc.eos.eutu55.utils.EUTU55Constants.X_FORWARDED_HOST;

@Component
@Slf4j
public class ResponseFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String correlationId = ((HttpServletRequest) servletRequest).getHeader(X_CORRELATION_ID);
        response.setHeader(X_CORRELATION_ID, StringUtils.hasLength(correlationId)?correlationId:UUID.randomUUID().toString());
        response.setHeader(X_FORWARDED_HOST, getLocalInternetAddress());
        response.setHeader(CONTENT_TYPE,  servletRequest.getContentType());
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private static String getLocalInternetAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.error("Unable to determine the Host Address with error {}", e.getMessage());
        }
        return "localhost";
    }
}
