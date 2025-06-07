package uk.gov.companieshouse.extensions.api.attachments.file;

import org.springframework.http.HttpStatus;

public class FileTransferApiClientResponse {

    private String fileId;
    private HttpStatus httpStatus;

    public String getFileId() {
        return fileId;
    }

    public FileTransferApiClientResponse fileId(String fileId) {
        this.fileId = fileId;
        return this;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public FileTransferApiClientResponse httpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }
}
