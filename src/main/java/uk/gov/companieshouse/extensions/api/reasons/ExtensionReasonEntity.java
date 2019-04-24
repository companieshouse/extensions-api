package uk.gov.companieshouse.extensions.api.reasons;

import uk.gov.companieshouse.extensions.api.attachments.Attachment;

import java.util.ArrayList;
import java.util.List;

public class ExtensionReasonEntity extends ExtensionReason {
    private List<Attachment> attachments = new ArrayList<>();

    public void addAttachment(Attachment attachment) {
        if (attachments != null) {
            attachments.add(attachment);
        }
    }

    public List<Attachment> getAttachemnts() {
        return  attachments;
    }

    public void setAttachments(List<Attachment>  attachments) {
        this. attachments =  attachments;
    }
}
