package uk.gov.companieshouse.extensions.api.config;

import com.mongodb.MongoClientOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.impl.EnvironmentReaderImpl;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * MongoDB Properties .
 */
@Configuration
public class ApplicationConfiguration {

    @Bean
    public EnvironmentReader environmentReader() {
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
        return () -> LocalDateTime.now();
    }

    @Bean
    public Supplier<String> randomUUID() {
        return () -> UUID.randomUUID().toString();
    }
}
