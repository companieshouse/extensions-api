package uk.gov.companieshouse.extensions.api.config;

import java.util.Map;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webmvc.error.DefaultErrorAttributes;
import org.springframework.boot.webmvc.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import uk.gov.companieshouse.extensions.api.authorization.CompanyAuthorizationInterceptor;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;
import uk.gov.companieshouse.extensions.api.logger.RequestLoggerInterceptor;
import uk.gov.companieshouse.extensions.api.requests.RequestsService;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    private final ApiLogger logger;
    private final RequestsService requestsService;

    public InterceptorConfig(ApiLogger logger, RequestsService requestsService) {
        this.logger = logger;
        this.requestsService = requestsService;
    }

    @Bean
    public RequestLoggerInterceptor requestLoggerInterceptor() {
        return new RequestLoggerInterceptor();
    }

    @Bean
    public CompanyAuthorizationInterceptor companyInterceptor(ApiLogger logger, RequestsService requestsService) {
        return new CompanyAuthorizationInterceptor(logger, requestsService);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLoggerInterceptor());
        registry.addInterceptor(companyInterceptor(logger, requestsService))
            .addPathPatterns("/company/*/extensions/requests", "/company/*/extensions/requests/**");
    }

    // This bean override allows us to control what fields are returned in a Spring error response
    // By returning null, no details are returned for errors, only the http status
    @Bean
    public ErrorAttributes errorAttributes() {
        return new DefaultErrorAttributes() {
            @Override
            public Map<String, Object> getErrorAttributes(WebRequest webRequest, ErrorAttributeOptions options) {
                return null;
            }
        };
    }
}
