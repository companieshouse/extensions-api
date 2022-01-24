package uk.gov.companieshouse.extensions.api.config;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
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
    @Value("${MONGO_CONNECTION_POOL_MIN_SIZE:0}")
    private Integer optionalMinSize;
    @Value("${MONGO_CONNECTION_MAX_IDLE_TIME:0}")
    private Long optionalMaxConnectionIdleTimeMS;
    @Value("${MONGO_CONNECTION_MAX_LIFE_TIME:0}")
    private Long optionalMaxConnectionLifeTimeMS;

    @Value("${EXTENSIONS_API_MONGODB_URL}")
    private String mongoDbConnectionString;

    /**
     * Create a {@link MongoClientSettings} .
     *
     * @return A {@link MongoClientSettings} .
     */
    @Bean
    public MongoClientSettings mongoClientOptions() {
        ConnectionString connectionString = new ConnectionString(mongoDbConnectionString);

        return MongoClientSettings.builder()
            .applyConnectionString(connectionString)
            .applyToConnectionPoolSettings(builder -> builder.minSize(optionalMinSize)
                .maxConnectionIdleTime(optionalMaxConnectionIdleTimeMS, TimeUnit.MILLISECONDS)
                .maxConnectionLifeTime(optionalMaxConnectionLifeTimeMS, TimeUnit.MILLISECONDS)).build();
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
