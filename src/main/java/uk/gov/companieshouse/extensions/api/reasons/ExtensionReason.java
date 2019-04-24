package uk.gov.companieshouse.extensions.api.reasons;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public abstract class ExtensionReason {

    private String etag;

    private String id;

    private String reason;

    private String id;

    @JsonProperty("additional_text")
    private String additionalText;

    @JsonProperty("start_on")
    private LocalDate startOn;

    @JsonProperty("end_on")
    private LocalDate endOn;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDate getStartOn() {
        return startOn;
    }

    public void setStartOn(LocalDate startOn) {
        this.startOn = startOn;
    }

    public LocalDate getEndOn() {
        return endOn;
    }

    public void setEndOn(LocalDate endOn) {
        this.endOn = endOn;
    }

    public String toString() {
      return "Extension reason: " + reason + " Additional text: " + additionalText + " Date start: " + startOn + " Date end: " + endOn;
    }
}
