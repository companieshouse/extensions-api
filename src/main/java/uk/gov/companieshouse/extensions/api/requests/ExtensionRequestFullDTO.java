package uk.gov.companieshouse.extensions.api.requests;

import uk.gov.companieshouse.service.links.Links;

import java.util.ArrayList;
import java.util.List;

public class ExtensionRequestFullDTO extends ExtensionRequestFull {

    // key = hash of reason id, value = uri of reason
    private final List<Links> reasons = new ArrayList<>();

    public List<Links> getReasons() {
        return reasons;
    }

    public void addReason(Links reason) {
        reasons.add(reason);
    }
}
