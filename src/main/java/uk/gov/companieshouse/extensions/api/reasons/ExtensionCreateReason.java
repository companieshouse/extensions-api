package uk.gov.companieshouse.extensions.api.reasons;

public class ExtensionCreateReason extends ExtensionReasonCommon {

    public String toString() {
        return "Extension create reason " + super.getReason() + " Additional text: " + super.getAdditionalText() + "  Date start: " + super.getStartOn() + "  Date end: " + super.getEndOn();
    }
}
