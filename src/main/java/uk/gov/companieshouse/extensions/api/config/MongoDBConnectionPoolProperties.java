package uk.gov.companieshouse.extensions.api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.environment.EnvironmentReader;

@Component
public class MongoDBConnectionPoolProperties {

    private static String MONGO_CONNECTION_POOL_MIN_SIZE_KEY = "MONGO_CONNECTION_POOL_MIN_SIZE";
    private static String MONGO_CONNECTION_MAX_IDLE_KEY = "MONGO_CONNECTION_MAX_IDLE_TIME";
    private static String MONGO_CONNECTION_MAX_LIFE_KEY = "MONGO_CONNECTION_MAX_LIFE_TIME";

    @Autowired
    private EnvironmentReader environmentReader;

    private int minSize;

    private int maxConnectionIdleTimeMS;

    private int maxConnectionLifeTimeMS;

    /**
     * Constructs the config using environment variables for
     * Mongo Connection Pool settings. Sets default values in case
     * the environment variables are not supplied.
     */
    public MongoDBConnectionPoolProperties() {
        Integer optionalMinSize = environmentReader.getOptionalInteger(MONGO_CONNECTION_POOL_MIN_SIZE_KEY);
        Integer optionalMaxConnectionIdleTimeMS = environmentReader.getOptionalInteger(MONGO_CONNECTION_MAX_IDLE_KEY);
        Integer optionalMaxConnectionLifeTimeMS = environmentReader.getOptionalInteger(MONGO_CONNECTION_MAX_LIFE_KEY);

        this.minSize = optionalMinSize != null ? optionalMinSize : 1;
        this.maxConnectionIdleTimeMS = optionalMaxConnectionIdleTimeMS != null ? optionalMaxConnectionIdleTimeMS : 0;
        this.maxConnectionLifeTimeMS = optionalMaxConnectionLifeTimeMS != null ? optionalMaxConnectionLifeTimeMS : 0;
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
