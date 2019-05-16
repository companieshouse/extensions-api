package uk.gov.companieshouse.extensions.api.logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestLoggerInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    private ApiLogger logger;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String companyNumber = getCompanyNumber(request);
        logger.setCompanyNumber(companyNumber);
        String requestPath = getRequestMessage(request);
        logger.info("Request received - " + requestPath);
        return true; //continue on to next handler
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (ex == null) {
            logger.info("Request finished - " + getRequestMessage(request));
        } else {
            logger.error(ex);
        }
        logger.removeCompanyNumber();
    }

    /**
     * This splits the uri by "/" and searches the uri for "company" and assumes that the company number follows it
     * e.g. /company/00006400/extensions/requests
     * @param request
     * @return company number or empty string if not found
     */
    private String getCompanyNumber(HttpServletRequest request) {
        String uri = request.getRequestURI();
        boolean isPreviousTokenCompany = false;
        for (String token : uri.split("/")) {
            if (isPreviousTokenCompany) {
                return token;
            }
            if ("company".equals(token)) {
                isPreviousTokenCompany = true;
            }
        }
        return "";
    }

    private String getRequestMessage(HttpServletRequest request) {
        return request.getMethod() + " " + request.getRequestURI();
    }
}
