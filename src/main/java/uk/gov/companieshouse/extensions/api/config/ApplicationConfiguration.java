package uk.gov.companieshouse.extensions.api.config;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Supplier;

import com.mongodb.MongoClientOptions;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ApplicationConfiguration {

    /**
     * Constructs the config using environment variables for
     * Mongo Connection Pool settings. Sets default values in case
     * the environment variables are not supplied.
     */
    @Value("${MONGO_CONNECTION_POOL_MIN_SIZE}")
    private Integer optionalMinSize;
    @Value("${MONGO_CONNECTION_MAX_IDLE_TIME}")
    private Integer optionalMaxConnectionIdleTimeMS;
    @Value("${MONGO_CONNECTION_MAX_LIFE_TIME}")
    private Integer optionalMaxConnectionLifeTimeMS;

    /**
     * Create a {@link MongoClientOptions} .
     *
     * @return A {@link MongoClientOptions} .
     */
    @Bean
    public MongoClientOptions mongoClientOptions() {
        MongoDBConnectionPoolProperties connectionPoolProperties = new MongoDBConnectionPoolProperties(
            optionalMinSize,
            optionalMaxConnectionIdleTimeMS,
            optionalMaxConnectionLifeTimeMS);
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
}
