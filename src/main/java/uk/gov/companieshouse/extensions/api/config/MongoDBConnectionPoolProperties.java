package uk.gov.companieshouse.extensions.api.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
class MongoDBConnectionPoolProperties {

    /**
     * Constructs the config using environment variables for
     * Mongo Connection Pool settings. Sets default values in case
     * the environment variables are not supplied.
     */
    @Value("${MONGO_CONNECTION_POOL_MIN_SIZE:1}")
    private Integer minSize;
    @Value("${MONGO_CONNECTION_MAX_IDLE_TIME:0}")
    private Long maxConnectionIdleTimeMS;
    @Value("${MONGO_CONNECTION_MAX_LIFE_TIME:0}")
    private Long maxConnectionLifeTimeMS;

    /**
     * Don't set a default here, we want the app to fail to start if mongo url is not supplied
     */
    @Value("${EXTENSIONS_API_MONGODB_URL}")
    private String mongoDbConnectionString;

    Integer getMinSize() {
        return minSize;
    }

    Long getMaxConnectionIdleTimeMS() {
        return maxConnectionIdleTimeMS;
    }

    Long getMaxConnectionLifeTimeMS() {
        return maxConnectionLifeTimeMS;
    }

    String getMongoDbConnectionString() {
        return mongoDbConnectionString;
    }
}
