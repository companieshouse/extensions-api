package uk.gov.companieshouse.extensions.api.config;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.impl.EnvironmentReaderImpl;

import java.util.Optional;

@Component
public class MongoDBConnectionPoolProperties {

    private Integer minSize;
    private int maxConnectionIdleTimeMS;
    private int maxConnectionLifeTimeMS;
    private String connectionString;


    /**
     * Constructs the config using environment variables for Mongo Connection Pool settings. Sets
     * default values in case the environment variables are not supplied.
     */
    public MongoDBConnectionPoolProperties() {
        EnvironmentReader reader = new EnvironmentReaderImpl();

        this.minSize =
            Optional.ofNullable(reader.getOptionalInteger("MONGO_CONNECTION_POOL_MIN_SIZE_KEY"))
                .orElse(1);
        this.maxConnectionIdleTimeMS =
            Optional.ofNullable(reader.getOptionalInteger("MONGO_CONNECTION_MAX_IDLE_KEY"))
                .orElse(0);
        this.maxConnectionLifeTimeMS =
            Optional.ofNullable(reader.getOptionalInteger("MONGO_CONNECTION_MAX_LIFE_KEY"))
                .orElse(0);
        this.connectionString = Optional.ofNullable(reader.getOptionalString("EXTENSIONS_API_MONGODB_URL"))
            .orElse("mongodb://mongo:27017");
    }

    public int getMinSize() {
        return minSize;
    }

    public int getMaxConnectionIdleTimeMS() {
        return maxConnectionIdleTimeMS;
    }

    public int getMaxConnectionLifeTimeMS() {
        return maxConnectionLifeTimeMS;
    }

    public String getConnectionString() {
        return connectionString;
    }
}
