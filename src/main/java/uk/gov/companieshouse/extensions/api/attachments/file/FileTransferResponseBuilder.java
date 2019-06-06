package uk.gov.companieshouse.extensions.api.attachments.file;

import java.io.IOException;

@FunctionalInterface
public interface FileTransferResponseBuilder<R, T> {

   R createResponse(T input) throws IOException;
}
