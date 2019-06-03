package uk.gov.companieshouse.extensions.api.attachments.file;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public class FileDownloaderResponse {

    private HttpStatus httpStatus;
    private HttpHeaders httpHeaders;

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public HttpHeaders getHttpHeaders() {
        return httpHeaders;
    }

    public void setHttpHeaders(HttpHeaders httpHeaders) {
        this.httpHeaders = httpHeaders;
    }
}
