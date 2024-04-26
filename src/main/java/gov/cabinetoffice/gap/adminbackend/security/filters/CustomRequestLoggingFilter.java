package gov.cabinetoffice.gap.adminbackend.security.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

@Component
@Slf4j
public class CustomRequestLoggingFilter extends AbstractRequestLoggingFilter {

    private final Set<String> excludedUrls = Set.of("/health");

    public CustomRequestLoggingFilter() {
        this.setIncludeHeaders(true);
        this.setIncludeQueryString(true);
        this.setMaxPayloadLength(10000);
        this.setIncludePayload(true);
    }

    @Override
    protected boolean shouldLog(HttpServletRequest request) {
        log.info("Request Url is : {}",request.getRequestURI());
        log.info("Is debug enabled : {}", logger.isDebugEnabled());

        if (excludedUrls.contains(request.getRequestURI())) {
            log.info("Is health endpoint {}",excludedUrls.contains(request.getRequestURI() ));
            return false;
        }

        return logger.isDebugEnabled();
    }

    @Override
    protected void beforeRequest(HttpServletRequest request, String message) {
        logger.debug(message);
    }

    @Override
    protected void afterRequest(HttpServletRequest request, String message) {
        logger.debug(message);
    }

}
