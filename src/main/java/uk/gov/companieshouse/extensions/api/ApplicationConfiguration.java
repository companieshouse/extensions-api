package uk.gov.companieshouse.extensions.api;

import com.mongodb.MongoClientOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * General application configuration .
 */
@Configuration
public class ApplicationConfiguration {

    @Autowired
    private MongoDbConnectionPoolProperties configuration;

    /**
     * Create a {@link MongoClientOptions} .
     *
     * @return A {@link MongoClientOptions} .
     */
    @Bean
    public MongoClientOptions mongoClientOptions() {
        return MongoClientOptions.builder().minConnectionsPerHost(Integer.valueOf(configuration.getMinSize()))
                .maxConnectionIdleTime(configuration.getMaxConnectionIdleTimeMS())
                .maxConnectionLifeTime(configuration.getMaxConnectionLifeTimeMS())
                .build();
    }
}
