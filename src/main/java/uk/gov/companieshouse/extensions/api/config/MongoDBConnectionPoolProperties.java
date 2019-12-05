package uk.gov.companieshouse.extensions.api.config;


public class MongoDBConnectionPoolProperties {

    private int minSize;

    private int maxConnectionIdleTimeMS;

    private int maxConnectionLifeTimeMS;

    public MongoDBConnectionPoolProperties(Integer optionalMinSize,
                                           Integer optionalMaxConnectionIdleTimeMS,
                                           Integer optionalMaxConnectionLifeTimeMS) {

        this.minSize = optionalMinSize != null ? optionalMinSize : 1;
        this.maxConnectionIdleTimeMS = optionalMaxConnectionIdleTimeMS != null ? optionalMaxConnectionIdleTimeMS : 0;
        this.maxConnectionLifeTimeMS = optionalMaxConnectionLifeTimeMS != null ? optionalMaxConnectionLifeTimeMS : 0;

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
}
