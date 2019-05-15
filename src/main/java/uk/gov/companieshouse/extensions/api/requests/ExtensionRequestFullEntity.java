package uk.gov.companieshouse.extensions.api.requests;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.core.mapping.Document;

import uk.gov.companieshouse.extensions.api.reasons.ExtensionReasonEntity;

@Document(collection = "extension_requests")
public class ExtensionRequestFullEntity extends ExtensionRequestFull {

    private List<ExtensionReasonEntity> reasons = new ArrayList<>();

    public void addReason(ExtensionReasonEntity extensionReason) {
        if (reasons != null) {
            reasons.add(extensionReason);
        }
    }

    public List<ExtensionReasonEntity> getReasons() {
        return reasons;
    }

    public void setReasons(List<ExtensionReasonEntity> reasons) {
        this.reasons = reasons;
    }

    public Optional<ExtensionReasonEntity> mapToReason(String reasonId) {
        return reasons.stream()
            .filter(reason -> reason.getId().equals(reasonId))
            .findAny();
    }
}
