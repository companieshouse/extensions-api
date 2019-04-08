package uk.gov.companieshouse.extensions.api.config;

import com.mongodb.MongoClientOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MongoDB Properties .
 */
@Configuration
public class MongoDBConfiguration {

    @Autowired
    private MongoDBConnectionPoolProperties connectionPoolProperties;

    /**
     * Create a {@link MongoClientOptions} .
     *
     * @return A {@link MongoClientOptions} .
     */
    @Bean
    public MongoClientOptions mongoClientOptions() {
        return MongoClientOptions.builder().minConnectionsPerHost(Integer.valueOf(connectionPoolProperties.getMinSize()))
                .maxConnectionIdleTime(connectionPoolProperties.getMaxConnectionIdleTimeMS())
                .maxConnectionLifeTime(connectionPoolProperties.getMaxConnectionLifeTimeMS())
                .build();
    }
}
