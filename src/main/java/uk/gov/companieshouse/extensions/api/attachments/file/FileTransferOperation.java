package uk.gov.companieshouse.extensions.api.attachments.file;

import java.io.IOException;

@FunctionalInterface
public interface FileTransferOperation<T> {

    T execute() throws IOException;
}
