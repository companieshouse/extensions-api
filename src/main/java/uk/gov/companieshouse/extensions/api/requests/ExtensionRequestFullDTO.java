package uk.gov.companieshouse.extensions.api.requests;

import uk.gov.companieshouse.service.links.Links;

public class ExtensionRequestFullDTO extends ExtensionRequestFull {

    // key = hash of reason id, value = uri of reason
    private Links reasons;

    public Links getReasons() {
        return reasons;
    }

    public void setReasons(Links reasons) {
        this.reasons = reasons;
    }
}
