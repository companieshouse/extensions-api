package uk.gov.companieshouse.extensions.api.requests;

import org.springframework.data.mongodb.core.mapping.Document;
import uk.gov.companieshouse.extensions.api.reasons.ExtensionReason;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "extension_requests")
public class ExtensionRequestFullEntity extends ExtensionRequestFull {

    private List<ExtensionReason> reasons = new ArrayList<>();

    public void addReason(ExtensionReason extensionReason) {
        if (reasons != null) {
            reasons.add(extensionReason);
        }
    }

    public List<ExtensionReason> getReasons() {
        return reasons;
    }

    public void setReasons(List<ExtensionReason> reasons) {
        this.reasons = reasons;
    }
}
