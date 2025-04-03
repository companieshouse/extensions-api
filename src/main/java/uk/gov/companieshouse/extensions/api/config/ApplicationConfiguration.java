package uk.gov.companieshouse.extensions.api.config;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;

import groovy.util.logging.Slf4j;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.http.ApiKeyHttpClient;

@Configuration
@Slf4j
public class ApplicationConfiguration {

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
    public Supplier<InternalApiClient> internalApiClientSupplier(
        @Value("${internal.api.key}") String internalApiKey,
        @Value("${file.transfer.api.url}") String fileTransferApiUrl) {
        return () -> {
            var httpClient = new ApiKeyHttpClient(internalApiKey);
            var internalApiClient = new InternalApiClient(httpClient);
            internalApiClient.setBasePath(fileTransferApiUrl);
            return internalApiClient;
        };
    }

    @Bean
    public Tika tika() {
        return new Tika();
    }

}
