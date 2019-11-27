package uk.gov.companieshouse.extensions.api.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RequestStatus {

    private Status status;

    @JsonProperty("is_auto_accepted")
    private boolean isAutoAccepted;

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean getIsAutoAccepted() {
        return isAutoAccepted;
    }

    public void setIsAutoAccepted(boolean isAutoAccepted) {
        this.isAutoAccepted = isAutoAccepted;
    }
}
