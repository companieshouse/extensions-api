package uk.gov.companieshouse.extensions.api.reasons;

import uk.gov.companieshouse.service.links.Links;

import java.time.LocalDate;

public class ExtensionReasonEntityBuilder {

    private String id;
    private Links links;
    private String reason;
    private String additionalText;
    private LocalDate startOn;
    private LocalDate endOn;

    public static ExtensionReasonEntityBuilder builder() {
        return new ExtensionReasonEntityBuilder();
    }

    public ExtensionReasonEntityBuilder withLinks(String requestURI) {
        if (this.id == null) {
            throw new UnsupportedOperationException("Links cannot be set before ID");
        }
        String linkToSelf = requestURI + "/" + this.id;
        Links links = new Links();
        links.setLink(() ->  "self", linkToSelf);
        this.links = links;
        return this;
    }

    public ExtensionReasonEntityBuilder withReason(String reason) {
        this.reason = reason;
        return this;
    }

    public ExtensionReasonEntityBuilder withAdditionalText(String additionalText) {
        this.additionalText = additionalText;
        return this;
    }

    public ExtensionReasonEntityBuilder withStartOn(LocalDate startOn) {
        this.startOn = startOn;
        return this;
    }

    public ExtensionReasonEntityBuilder withEndOn(LocalDate endOn) {
        this.endOn = endOn;
        return this;
    }

    public ExtensionReasonEntityBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public ExtensionReasonEntity build() {
        ExtensionReasonEntity extensionReasonEntity
            = new ExtensionReasonEntity();
        extensionReasonEntity.setId(this.id);
        extensionReasonEntity.setLinks(this.links);
        extensionReasonEntity.setReason(this.reason);
        extensionReasonEntity.setAdditionalText(this.additionalText);
        extensionReasonEntity.setStartOn(this.startOn);
        extensionReasonEntity.setEndOn(this.endOn);
        return extensionReasonEntity;
    }
}
