package uk.gov.companieshouse.extensions.api.config;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import uk.gov.companieshouse.extensions.api.authorization.CompanyAuthorizationInterceptor;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;
import uk.gov.companieshouse.extensions.api.logger.RequestLoggerInterceptor;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Autowired
    private ApiLogger logger;

    @Value("${FEATURE_AUTHORISATION}")
    private String authorisationActive;

    @Bean
    public RequestLoggerInterceptor requestLoggerInterceptor() {
        return new RequestLoggerInterceptor();
    }

    @Bean
    public CompanyAuthorizationInterceptor companyInterceptor(ApiLogger logger) {
        return new CompanyAuthorizationInterceptor(logger);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLoggerInterceptor());
        if (authorisationActive.equals("true")) {
            registry.addInterceptor(companyInterceptor(logger))
                .excludePathPatterns("/**/error");
        }
    }

    // This bean override allows us to control what fields are returned in a Spring error response
    // By returning null, no details are returned for errors, only the http status
    @Bean
    public ErrorAttributes errorAttributes() {
        return new DefaultErrorAttributes() {
            @Override
            public Map<String, Object> getErrorAttributes(WebRequest request, boolean includeStackTrace) {
                return null;
            }
        };
    }
}