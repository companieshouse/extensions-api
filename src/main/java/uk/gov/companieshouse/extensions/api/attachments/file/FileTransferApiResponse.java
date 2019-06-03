package uk.gov.companieshouse.extensions.api.attachments.file;

/**
 * Wrapper class for the info returned from the File Transfer API
 */
public class FileTransferApiResponse {

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
