package uk.gov.companieshouse.extensions.api.attachments.file;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Actor Not Found")
public class FileTransferURIValidationException extends RuntimeException {

    public FileTransferURIValidationException(String message, Exception e) {
        super(message, e);
    }
}
