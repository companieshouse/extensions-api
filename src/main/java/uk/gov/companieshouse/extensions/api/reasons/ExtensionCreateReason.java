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

    @JsonProperty("ill_person")
    private String illPerson;

    @JsonProperty("continued_illness")
    private String continuedIllness;

    @JsonProperty("illness_information")
    private String illnessInformation;

    @JsonProperty("other_information")
    private String otherInformation;

    public String getIllPerson() {
        return illPerson;
    }

    public void setIllPerson(String illPerson) {
        this.illPerson = illPerson;
    }

    public String getContinuedIllness() {
        return continuedIllness;
    }

    public void setContinuedIllness(String continuedIllness) {
        this.continuedIllness = continuedIllness;
    }

    public String getIllnessInformation() {
        return illnessInformation;
    }

    public void setIllnessInformation(String illnessInformation) {
        this.illnessInformation = illnessInformation;
    }

    public String getOtherInformation() {
        return otherInformation;
    }

    public void setOtherInformation(String otherInformation) {
        this.otherInformation = otherInformation;
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
