package uk.gov.companieshouse.extensions.api.reasons;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import uk.gov.companieshouse.service.links.Links;

public class ExtensionReasonDTO extends ExtensionReason {
    // key = hash of attachment id, value = uri of attachment
    @JsonUnwrapped
    private Links attachments;

    public Links getAttachments() {
        return attachments;
    }

    public void setAttachments(Links attachments) {
        this.attachments = attachments;
    }

}
