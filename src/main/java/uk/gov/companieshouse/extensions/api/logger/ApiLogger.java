package uk.gov.companieshouse.extensions.api.logger;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.extensions.api.requests.ERICHeaderParser;
import uk.gov.companieshouse.logging.Logger;

@Component
public class ApiLogger {

    private static final ThreadLocal<String> COMPANY_NUMBER = new ThreadLocal<>();

    private final Logger logger;
    private final ERICHeaderParser ericHeaderParser;

    @Autowired
    public ApiLogger(Logger logger, ERICHeaderParser ericHeaderParser) {
        this.logger = logger;
        this.ericHeaderParser = ericHeaderParser;
    }

    public void setCompanyNumber(String companyNumber) {
        COMPANY_NUMBER.set(companyNumber);
    }

    public void removeCompanyNumber() {
        COMPANY_NUMBER.remove();
    }

    /**
     * Populates the default data that needs to be logged
     */
    public Map<String, Object> getInternalDataMap() {
        Map<String, Object> logData = new HashMap<>();
        logData.put("company_number", COMPANY_NUMBER.get());
        logData.put("thread_id", Thread.currentThread().threadId());
        return logData;
    }

    private Map<String, Object> createDataMap(Map<String, Object> values) {
        Map<String, Object> defaultValues = getInternalDataMap();
        defaultValues.putAll(values);
        return defaultValues;
    }

    public void debug(String message) {
        logger.debug(message, getInternalDataMap());
    }

    /**
     * Will extract a userid from the eric header attached to a request.
     */
    public void debug(String message, HttpServletRequest request) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("user_id", ericHeaderParser.getUserId(request));
        debug(message, logData);
    }

    public void debug(String message, Map<String, Object> values) {
        Map<String, Object> dataMap = createDataMap(values);
        logger.debug(message, dataMap);
    }

    public void info(String message) {
        logger.info(message, getInternalDataMap());
    }

    public void info(String message, Map<String, Object> values) {
        logger.info(message, createDataMap(values));
    }

    public void error(Exception e) {
        logger.error(e.getMessage(), e, getInternalDataMap());
    }

    public void error(String message) {
        logger.error(message, getInternalDataMap());
    }

    public void error(String message, Exception e) {
        logger.error(message, e, getInternalDataMap());
    }

    public void error(String message, Map<String, Object> values) {
        logger.error(message, createDataMap(values));
    }
}
