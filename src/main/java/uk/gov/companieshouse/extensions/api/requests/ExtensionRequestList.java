package uk.gov.companieshouse.extensions.api.requests;

import uk.gov.companieshouse.extensions.api.response.ListResponse;
import java.util.List;

public class ExtensionRequestList extends ListResponse {

    private List<ExtensionRequestFull> items;

    public List<ExtensionRequestFull> getItems() {
        return items;
    }

    public void setItems(List<ExtensionRequestFull> items) {
        this.items = items;
    }
}
