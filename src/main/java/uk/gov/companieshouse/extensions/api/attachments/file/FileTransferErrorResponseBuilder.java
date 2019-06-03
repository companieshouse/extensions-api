package uk.gov.companieshouse.extensions.api.attachments.file;

import org.springframework.http.HttpStatus;

@FunctionalInterface
public interface FileTransferErrorResponseBuilder<T> {

    T createErrorResponse(HttpStatus httpStatus);
}
