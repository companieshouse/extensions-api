package uk.gov.companieshouse.extensions.api.attachments.upload;

public class FileTransferGatewayResponse {

    private String fileId;
    private boolean inError;
    private String errorStatusCode;
    private String errorStatusText;
    private String errorMessage;


    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public boolean isInError() {
        return inError;
    }

    public void setInError(boolean inError) {
        this.inError = inError;
    }

    public String getErrorStatusCode() {
        return errorStatusCode;
    }

    public void setErrorStatusCode(String errorStatusCode) {
        this.errorStatusCode = errorStatusCode;
    }

    public String getErrorStatusText() {
        return errorStatusText;
    }

    public void setErrorStatusText(String errorStatusText) {
        this.errorStatusText = errorStatusText;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
