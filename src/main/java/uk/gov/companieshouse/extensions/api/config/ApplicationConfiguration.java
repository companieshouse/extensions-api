package uk.gov.companieshouse.extensions.api.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.api.handler.filetransfer.FileTransferHttpClient;
import uk.gov.companieshouse.api.handler.filetransfer.InternalFileTransferClient;
import uk.gov.companieshouse.extensions.api.Application;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Configuration
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
    public LocalDateTime dateTimeNow() {
        return LocalDateTime.now();
    }

    @Bean
    public Supplier<UUID> randomUUID() {
        return UUID::randomUUID;
    }

    // @Bean
    // public RestTemplate restTemplate(RestTemplateBuilder builder) {
    //     return builder.build();
    // }

    @Bean
    public Supplier<InternalFileTransferClient> internalFileTransferClient(
        @Value("${internal.api.key}") String internalApiKey,
        @Value("${file.transfer.api.url}") String fileTransferApiUrl) {
        return () -> {
            var httpClient = new FileTransferHttpClient(internalApiKey);
            var internalApiClient = new InternalFileTransferClient(httpClient);
            internalApiClient.setBasePath(fileTransferApiUrl);
            return internalApiClient;
        };
    }

    @Bean
    public Tika tika() {
        return new Tika();
    }

    @Bean
    public Logger getLogger() {
        return LoggerFactory.getLogger(Application.APP_NAMESPACE);
    }
}
