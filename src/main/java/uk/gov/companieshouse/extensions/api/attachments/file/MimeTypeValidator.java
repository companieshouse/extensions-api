package uk.gov.companieshouse.extensions.api.attachments.file;

import java.util.Arrays;
import java.util.List;

public class MimeTypeValidator {

    private MimeTypeValidator() {
    }

    protected static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
        "image/png",
        "image/jpeg",
        "image/jpg",
        "text/csv",
        "application/pdf",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "image/gif",
        "application/x-tar",
        "application/x-7z-compressed",
        "application/zip"
    );

    protected static boolean isValidMimeType(final String mimeType) {
        return ALLOWED_MIME_TYPES.contains(mimeType);
    }

}
