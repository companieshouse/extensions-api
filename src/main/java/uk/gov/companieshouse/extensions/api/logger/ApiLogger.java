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
    private Map<String, Object> getDefaultDataMap() {
        Map<String, Object> logData = new HashMap<>();
        logData.put("company_number", COMPANY_NUMBER.get());
        logData.put("thread_id", Thread.currentThread().threadId());
        return logData;
    }

    private Map<String, Object> getDataMap(Map<String, Object> values) {
        Map<String, Object> defaultValues = getDefaultDataMap();
        defaultValues.putAll(values);
        return defaultValues;
    }

    public void debug(String message) {
        logger.debug(message, getDefaultDataMap());
    }

    /**
     * Will extract a userid from the eric header attached to a request.
     * @param message
     * @param request
     */
    public void debug(String message, HttpServletRequest request) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("user_id", ericHeaderParser.getUserId(request));
        debug(message, logData);
    }

    public void debug(String message, Map<String, Object> values) {
        logger.debug(message, getDataMap(values));
    }

    public void info(String message) {
        logger.info(message, getDefaultDataMap());
    }

    public void info(String message, Map<String, Object> values) {
        logger.info(message, getDataMap(values));
    }

    public void error(Exception e) {
        logger.error(e.getMessage(), e, getDefaultDataMap());
    }

    public void error(String message) {
        logger.error(message, getDefaultDataMap());
    }

    public void error(String message, Exception e) {
        logger.error(message, e, getDefaultDataMap());
    }

    public void error(String message, Map<String, Object> values) {
        logger.error(message, getDataMap(values));
    }
}
