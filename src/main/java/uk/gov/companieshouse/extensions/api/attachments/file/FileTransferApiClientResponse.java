package uk.gov.companieshouse.extensions.api.attachments.file;

import org.springframework.http.HttpStatus;

public class FileTransferApiClientResponse {

    private String fileId;
    private HttpStatus httpStatus;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }
}
