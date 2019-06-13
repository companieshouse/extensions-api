package uk.gov.companieshouse.extensions.api.attachments.file;

import java.io.IOException;

@FunctionalInterface
public interface FileTransferResponseBuilder<T> {

   FileTransferApiClientResponse createResponse(T input) throws IOException;
}
