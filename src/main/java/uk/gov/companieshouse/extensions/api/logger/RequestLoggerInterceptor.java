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
    }

    private String getRequestMessage(HttpServletRequest request) {
        return request.getMethod() + " " + request.getRequestURI();
    }
}
