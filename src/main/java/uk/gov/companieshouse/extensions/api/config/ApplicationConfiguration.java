package uk.gov.companieshouse.extensions.api.config;

import com.mongodb.MongoClientOptions;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.impl.EnvironmentReaderImpl;
import uk.gov.companieshouse.extensions.api.logger.RequestLoggerInterceptor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * MongoDB Properties .
 */
@Configuration
public class ApplicationConfiguration implements WebMvcConfigurer {

    @Bean
    private EnvironmentReader environmentReader() {
        return new EnvironmentReaderImpl();
    }

    /**
     * Create a {@link MongoClientOptions} .
     *
     * @return A {@link MongoClientOptions} .
     */
    @Bean
    public MongoClientOptions mongoClientOptions() {
        MongoDBConnectionPoolProperties connectionPoolProperties = new MongoDBConnectionPoolProperties(environmentReader());
        return MongoClientOptions.builder().minConnectionsPerHost(connectionPoolProperties.getMinSize())
                .maxConnectionIdleTime(connectionPoolProperties.getMaxConnectionIdleTimeMS())
                .maxConnectionLifeTime(connectionPoolProperties.getMaxConnectionLifeTimeMS())
                .build();
    }

    @Bean
    public Supplier<LocalDateTime> dateTimeNow() {
        return LocalDateTime::now;
    }

    @Bean
    public Supplier<String> randomUUID() {
        return () -> UUID.randomUUID().toString();
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    private RequestLoggerInterceptor requestLoggerInterceptor() {
        return new RequestLoggerInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLoggerInterceptor());
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
