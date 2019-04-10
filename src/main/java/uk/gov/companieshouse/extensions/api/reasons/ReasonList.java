package uk.gov.companieshouse.extensions.api.reasons;

import uk.gov.companieshouse.extensions.api.response.ListResponse;

import java.util.List;

public class ReasonList extends ListResponse {

    private List<ExtensionReason> items;

    public List<ExtensionReason> getItems() {
        return items;
    }

    public void setItems(List<ExtensionReason> items) {
        this.items = items;
    }
}
