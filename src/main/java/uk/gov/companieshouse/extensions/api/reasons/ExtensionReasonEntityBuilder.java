package uk.gov.companieshouse.extensions.api.reasons;

import java.time.LocalDate;

public class ExtensionReasonEntityBuilder {

    private String reason;
    private String additionalText;
    private LocalDate startOn;
    private LocalDate endOn;

    public static ExtensionReasonEntityBuilder getInstance() {
        return new ExtensionReasonEntityBuilder();
    }


    public ExtensionReasonEntityBuilder withReason(String reason){
        this.reason = reason;
        return this;
    }

    public ExtensionReasonEntityBuilder withAdditionalText(String additionalText){
        this.additionalText = additionalText;
        return this;
    }

    public ExtensionReasonEntityBuilder withStartOn(LocalDate startOn){
        this.startOn = startOn;
        return this;
    }

    public ExtensionReasonEntityBuilder withEndOn(LocalDate endOn){
        this.endOn = endOn;
        return this;
    }

    public ExtensionReasonEntity build() {
        ExtensionReasonEntity extensionReasonEntity
            = new ExtensionReasonEntity();
        extensionReasonEntity.setReason(this.reason);
        extensionReasonEntity.setAdditionalText(this.additionalText);
        extensionReasonEntity.setStartOn(this.startOn);
        extensionReasonEntity.setEndOn(this.endOn);
        return extensionReasonEntity;
    }
}
