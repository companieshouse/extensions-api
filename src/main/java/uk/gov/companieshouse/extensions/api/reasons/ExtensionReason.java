package uk.gov.companieshouse.extensions.api.reasons;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.companieshouse.service.links.Links;

public abstract class ExtensionReason {

    private String etag;

    private String reason;

    @JsonProperty("additional_text")
    private String additionalText;

    @JsonProperty("start_on")
    private String startOn;

    @JsonProperty("end_on")
    private String endOn;

    // key = hash of attachment id, value = uri of attachment
    private Links attachments;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAdditionalText() {
        return additionalText;
    }

    public void setAdditionalText(String additionalText) {
        this.additionalText = additionalText;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getStartOn() {
        return startOn;
    }

    public void setStartOn(String startOn) {
        this.startOn = startOn;
    }

    public Links getAttachments() {
        return this.attachments;
    }

    public void setAttachments(Links attachments) {
        this.attachments = attachments;
    }

    public String getEndOn() {
        return endOn;
    }

    public void setEndOn(String endOn) {
        this.endOn = endOn;
    }

    public String toString() {
      return "Extension reason " + reason + " Additional text: " + additionalText + "  Date start: " + startOn + "  Date end: " + endOn;
    }
}
