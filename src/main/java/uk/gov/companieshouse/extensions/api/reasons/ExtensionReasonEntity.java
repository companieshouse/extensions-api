package uk.gov.companieshouse.extensions.api.reasons;

import uk.gov.companieshouse.extensions.api.attachments.Attachment;

import java.util.ArrayList;
import java.util.List;

public class ExtensionReasonEntity extends ExtensionReason {
    private List<Attachment>  attachments = new ArrayList<>();

    public void addReason(Attachment attachment) {
        if (attachments != null) {
            attachments.add(attachment);
        }
    }

    public List<Attachment> getReasons() {
        return  attachments;
    }

    public void setReasons(List<Attachment>  attachments) {
        this. attachments =  attachments;
    }
}
