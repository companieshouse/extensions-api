package uk.gov.companieshouse.extensions.api.logger;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.extensions.api.Application;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Component
public class ApiLogger {
    private static final Logger LOG = LoggerFactory.getLogger(Application.APP_NAMESPACE);
    private static final ThreadLocal<String> COMPANY_NUMBER = new ThreadLocal<>();

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
        logData.put("thread_id", Thread.currentThread().getId());
        return logData;
    }

    private Map<String, Object> getDataMap(Map<String, Object> values) {
        Map<String, Object> defaultValues = getDefaultDataMap();
        defaultValues.putAll(values);
        return defaultValues;
    }

    public void debug(String message) {
        LOG.debug(message, getDefaultDataMap());
    }

    public void debug(String message, Map<String, Object> values) {
        LOG.debug(message, getDataMap(values));
    }

    public void info(String message) {
        LOG.info(message, getDefaultDataMap());
    }

    public void info(String message, Map<String, Object> values) {
        LOG.info(message, getDataMap(values));
    }

    public void error(Exception e) {
        LOG.error(e.getMessage(), e, getDefaultDataMap());
    }

    public void error(String message) {
        LOG.error(message, getDefaultDataMap());
    }

    public void error(String message, Exception e) {
        LOG.error(message, e, getDefaultDataMap());
    }

    public void error(String message, Map<String, Object> values) {
        LOG.error(message, getDataMap(values));
    }
}
