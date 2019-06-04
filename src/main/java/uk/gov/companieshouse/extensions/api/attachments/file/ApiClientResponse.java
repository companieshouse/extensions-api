package uk.gov.companieshouse.extensions.api.attachments.file;

import org.springframework.http.HttpStatus;

public interface ApiClientResponse {

    HttpStatus getHttpStatus();

    void setHttpStatus(HttpStatus httpStatus);

}
