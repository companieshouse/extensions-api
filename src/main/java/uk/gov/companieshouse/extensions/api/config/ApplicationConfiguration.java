package uk.gov.companieshouse.extensions.api.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import groovy.util.logging.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Configuration
@Slf4j
public class ApplicationConfiguration {
    @Autowired
    private ApiLogger logger;

    /**
     * Set Mongo client settings
     */
    @Bean
    public MongoClientSettings mongoClientSettings(MongoDBConnectionPoolProperties connectionPoolProperties) {
        ConnectionString connectionString = new ConnectionString(connectionPoolProperties.getMongoDbConnectionString());

        return MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .applyToConnectionPoolSettings(builder -> builder
                .minSize(connectionPoolProperties.getMinSize())
                .maxConnectionIdleTime(connectionPoolProperties.getMaxConnectionIdleTimeMS(), TimeUnit.MILLISECONDS)
                .maxConnectionLifeTime(connectionPoolProperties.getMaxConnectionLifeTimeMS(), TimeUnit.MILLISECONDS))
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

}
