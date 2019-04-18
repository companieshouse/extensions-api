package uk.gov.companieshouse.extensions.api.reasons;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.companieshouse.extensions.api.attachments.AttachmentsMetadata;
import uk.gov.companieshouse.service.links.Links;

import java.util.List;

public abstract class ExtensionReason {

    private String id;

    private String etag;

    private String reason;

    @JsonProperty("additional_text")
    private String additionalText;

    @JsonProperty("start_on")
    private String startOn;

    @JsonProperty("end_on")
    private String endOn;

    public void addAttachment(AttachmentsMetadata attachment) {
        if (attachments != null) {
            attachments.add(attachment);
        }
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // key = hash of attachment id, value = uri of attachment
    private List<AttachmentsMetadata> attachments;

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

    public String getEndOn() {
        return endOn;
    }

    public void setEndOn(String endOn) {
        this.endOn = endOn;
    }

    public List<AttachmentsMetadata> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentsMetadata> attachments) {
        this.attachments = attachments;
    }

    public String toString() {
      return "Extension reason " + reason + " Additional text: " + additionalText + "  Date start: " + startOn + "  Date end: " + endOn;
    }
}
