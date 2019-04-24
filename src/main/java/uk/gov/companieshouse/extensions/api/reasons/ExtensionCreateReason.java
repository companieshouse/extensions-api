package uk.gov.companieshouse.extensions.api.reasons;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class ExtensionCreateReason {
    private String reason;

    @JsonProperty("additional_text")
    private String additionalText;

    @JsonProperty("start_on")
    private LocalDate startOn;

    @JsonProperty("end_on")
    private LocalDate endOn;

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
        return "Extension create reason " + reason + " Additional text: " + additionalText + "  Date start: " + startOn + "  Date end: " + endOn;
    }
}
