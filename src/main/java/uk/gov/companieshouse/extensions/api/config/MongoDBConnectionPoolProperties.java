package uk.gov.companieshouse.extensions.api.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MongoDBConnectionPoolProperties {

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

    Integer getMinSize() {
        return minSize;
    }

    Long getMaxConnectionIdleTimeMS() {
        return maxConnectionIdleTimeMS;
    }

    Long getMaxConnectionLifeTimeMS() {
        return maxConnectionLifeTimeMS;
    }

}
