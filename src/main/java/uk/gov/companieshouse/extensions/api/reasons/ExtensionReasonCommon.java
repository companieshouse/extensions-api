package uk.gov.companieshouse.extensions.api.reasons;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public abstract class ExtensionReasonCommon {
    private String reason;

    @JsonProperty("start_on")
    private LocalDate startOn;

    @JsonProperty("end_on")
    private LocalDate endOn;

    @JsonProperty("affected_person")
    private String affectedPerson;

    @JsonProperty("continued_illness")
    private String continuedIllness;

    @JsonProperty("reason_information")
    private String reasonInformation;

    public String getAffectedPerson() {
        return affectedPerson;
    }

    public void setAffectedPerson(String affectedPerson) {
        this.affectedPerson = affectedPerson;
    }

    public String getContinuedIllness() {
        return continuedIllness;
    }

    public void setContinuedIllness(String continuedIllness) {
        this.continuedIllness = continuedIllness;
    }

    public String getReasonInformation() {
        return reasonInformation;
    }

    public void setReasonInformation(String reasonInformation) {
        this.reasonInformation = reasonInformation;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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
}
