package uk.gov.companieshouse.extensions.api.reasons;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.companieshouse.service.links.Links;

import java.time.LocalDate;

public abstract class ExtensionReason {

    private String etag;

    private String id;

    private String reason;

    @JsonProperty("additional_text")
    private String additionalText;

    @JsonProperty("start_on")
    private LocalDate startOn;

    @JsonProperty("end_on")
    private LocalDate endOn;

    @JsonProperty("affected_person")
    private String affectedPerson;

    @JsonProperty("reason_information")
    private String reasonInformation;

    @JsonProperty("continued_illness")
    private String continuedIllness;

    private Links links;

    public String getAffectedPerson() {
        return affectedPerson;
    }

    public void setAffectedPerson(String affectedPerson) {
        this.affectedPerson = affectedPerson;
    }

    public String getReasonInformation() {
        return reasonInformation;
    }

    public void setReasonInformation(String reasonInformation) {
        this.reasonInformation = reasonInformation;
    }

    public String getContinuedIllness() {
        return continuedIllness;
    }

    public void setContinuedIllness(String continuedIllness) {
        this.continuedIllness = continuedIllness;
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

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    public String toString() {
      return "Extension reason: " + reason + " Additional text: " + additionalText + " Date start: " + startOn + " Date end: " + endOn;
    }
}
