package uk.gov.companieshouse.extensions.api.config;

import java.util.concurrent.TimeUnit;

import com.mongodb.MongoClientSettings;
import org.springframework.boot.mongodb.autoconfigure.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoClientConfiguration implements MongoClientSettingsBuilderCustomizer {

    private final MongoDBConnectionPoolProperties mongoDBConnectionPoolProperties;

    public MongoClientConfiguration(MongoDBConnectionPoolProperties mongoDBConnectionPoolProperties) {
        this.mongoDBConnectionPoolProperties = mongoDBConnectionPoolProperties;
    }

    @Override
    public void customize(MongoClientSettings.Builder builder) {
        builder.applyToConnectionPoolSettings(connectionPoolBuilder -> {
            connectionPoolBuilder.minSize(mongoDBConnectionPoolProperties.getMinSize());
            connectionPoolBuilder.maxConnectionIdleTime(mongoDBConnectionPoolProperties.getMaxConnectionIdleTimeMS(), TimeUnit.MILLISECONDS);
            connectionPoolBuilder.maxConnectionLifeTime(mongoDBConnectionPoolProperties.getMaxConnectionLifeTimeMS(), TimeUnit.MILLISECONDS);
        });
    }
}
