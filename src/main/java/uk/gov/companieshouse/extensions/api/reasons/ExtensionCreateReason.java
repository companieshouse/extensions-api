package uk.gov.companieshouse.extensions.api.reasons;

public class ExtensionCreateReason extends ExtensionReasonCommon {

    public String toString() {
        return "Extension create reason " + super.getReason() + " Reason information: " + super.getReasonInformation() + "  Date start: " + super.getStartOn() + "  Date end: " + super.getEndOn();
    }
}
