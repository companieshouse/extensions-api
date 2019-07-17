package uk.gov.companieshouse.extensions.api.requests;

public class RequestStatus {

    private Status status;
    private int numGrantedExtensionReqs;

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getNumGrantedExtensionReqs() {
        return numGrantedExtensionReqs;
    }

    public void setNumGrantedExtensionReqs(int numGrantedExtensionReqs) {
        this.numGrantedExtensionReqs = numGrantedExtensionReqs;
    }
}
