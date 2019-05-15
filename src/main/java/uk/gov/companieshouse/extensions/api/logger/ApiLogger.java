package uk.gov.companieshouse.extensions.api.logger;

import uk.gov.companieshouse.extensions.api.Application;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.Map;

public class ApiLogger {
    private static final Logger LOG = LoggerFactory.getLogger(Application.APP_NAMESPACE);

    private ApiLogger() {}

    private static String prefix(String message) {
        return "Thread id " + Thread.currentThread().getId() + " - " + message;
    }

    public static void debug(String message) {
        LOG.debug(prefix(message));
    }

    public static void debug(String message, Map<String, Object> values) {
        LOG.debug(prefix(message), values);
    }

    public static void info(String message) {
        LOG.info(prefix(message));
    }

    public static void info(String companyNumber, String message) {
        LOG.info(prefix(companyNumber + " - " + message));
    }

    public static void info(String message, Map<String, Object> values) {
        LOG.info(prefix(message), values);
    }

    public static void error(Exception e) {
        LOG.error(prefix(" "), e);
    }

    public static void error(String message, Exception e) {
        LOG.error(prefix(message), e);
    }

    public static void error(String message, Map<String, Object> values) {
        LOG.error(prefix(message), values);
    }
}
