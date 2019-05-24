package uk.gov.companieshouse.extensions.api.requests;

import uk.gov.companieshouse.service.links.CoreLinkKeys;
import uk.gov.companieshouse.service.links.LinkKey;

public enum ExtensionsLinkKeys implements LinkKey {
    DOWNLOAD("download"),
    SELF(CoreLinkKeys.SELF.key());

    private String key;

    private ExtensionsLinkKeys(String key) {
        this.key = key;
    }

    public String key() {
        return this.key;
    }
}
