package uk.gov.companieshouse.extensions.api.config;

import org.springframework.stereotype.Component;

@Component
public class MongoDBConnectionPoolProperties {

    private static String MONGO_CONNECTION_POOL_MIN_SIZE_KEY = "MONGO_CONNECTION_POOL_MIN_SIZE";
    private static String MONGO_CONNECTION_MAX_IDLE_KEY = "MONGO_CONNECTION_MAX_IDLE_TIME";
    private static String MONGO_CONNECTION_MAX_LIFE_KEY = "MONGO_CONNECTION_MAX_LIFE_TIME";

    private int minSize;

    private int maxConnectionIdleTimeMS;

    private int maxConnectionLifeTimeMS;

    /**
     * Constructs the config using environment variables for
     * Mongo Connection Pool settings. Sets default values in case
     * the environment variables are not supplied.
     */
    public MongoDBConnectionPoolProperties() {
        this.minSize = System.getenv(MONGO_CONNECTION_POOL_MIN_SIZE_KEY) != null ?
                Integer.valueOf(System.getenv(MONGO_CONNECTION_POOL_MIN_SIZE_KEY)) : 1;
        this.maxConnectionIdleTimeMS = System.getenv(MONGO_CONNECTION_MAX_IDLE_KEY) != null ?
                Integer.valueOf(System.getenv(MONGO_CONNECTION_MAX_IDLE_KEY)) : 0;
        this.maxConnectionLifeTimeMS = System.getenv(MONGO_CONNECTION_MAX_LIFE_KEY) != null ?
                Integer.valueOf(System.getenv(MONGO_CONNECTION_MAX_LIFE_KEY)) : 0;
    }

    public int getMinSize() {
        return minSize;
    }

    public void setMinSize(int minSize) {
        this.minSize = minSize;
    }

    public int getMaxConnectionIdleTimeMS() {
        return maxConnectionIdleTimeMS;
    }

    public void setMaxConnectionIdleTimeMS(int maxConnectionIdleTimeMS) {
        this.maxConnectionIdleTimeMS = maxConnectionIdleTimeMS;
    }

    public int getMaxConnectionLifeTimeMS() {
        return maxConnectionLifeTimeMS;
    }

    public void setMaxConnectionLifeTimeMS(int maxConnectionLifeTimeMS) {
        this.maxConnectionLifeTimeMS = maxConnectionLifeTimeMS;
    }
}
