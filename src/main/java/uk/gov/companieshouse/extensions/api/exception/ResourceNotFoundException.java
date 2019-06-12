package uk.gov.companieshouse.extensions.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception that is thrown when a particular resource is not found
 */
public class ResourceNotFoundException extends Exception {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public HttpStatus getStatusCode() {
        return HttpStatus.NOT_FOUND;
    }
}