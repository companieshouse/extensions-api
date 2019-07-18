package uk.gov.companieshouse.extensions.api.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestStatus {

    private Status status;
    @JsonProperty("num_granted_extension_reqs")
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
