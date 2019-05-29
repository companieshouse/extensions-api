package uk.gov.companieshouse.extensions.api.reasons;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import uk.gov.companieshouse.service.links.Links;

@JsonPropertyOrder({"etag", "id", "reason", "links", "attachments", "start_on", "end_on", "affected_person", "reason_information", "continued_illness"})
public abstract class ExtensionReason extends ExtensionReasonCommon {

    private String etag;

    private String id;

    @JsonUnwrapped
    private Links links;

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

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    public String toString() {
      return "Extension reason: " + super.getReason() + " Reason information: " + super.getReasonInformation() + " Date start: " + super.getStartOn() + " Date end: " + super.getEndOn();
    }
}
