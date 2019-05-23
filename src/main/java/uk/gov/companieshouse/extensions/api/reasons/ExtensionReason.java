package uk.gov.companieshouse.extensions.api.reasons;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import uk.gov.companieshouse.service.links.Links;

@JsonPropertyOrder({"etag", "id", "reason", "links", "attachments", "additional_text", "start_on", "end_on", "affected_person", "reason_information", "continued_illness"})
public abstract class ExtensionReason extends ExtensionCreateReason {

    private String etag;

    private String id;

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
      return "Extension reason: " + super.getReason() + " Additional text: " + super.getAdditionalText() + " Date start: " + super.getStartOn() + " Date end: " + super.getEndOn();
    }
}
