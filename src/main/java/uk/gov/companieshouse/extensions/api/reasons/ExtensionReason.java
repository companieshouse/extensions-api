package uk.gov.companieshouse.extensions.api.reasons;

import com.fasterxml.jackson.annotation.JsonProperty;

import uk.gov.companieshouse.extensions.api.attachments.Attachment;

import java.util.List;

public class ExtensionReason {

    private String reason;

    @JsonProperty("additional_text")
    private String additionalText;

    @JsonProperty("date_start")
    private String dateStart;

    @JsonProperty("date_end")
    private String dateEnd;

    private List<Attachment> attachments;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAdditionalText() {
        return additionalText;
    }

    public void setAdditionalText(String additionalText) {
        this.additionalText = additionalText;
    }

    public String getDateStart() {
        return dateStart;
    }

    public void setDateStart(String dateStart) {
        this.dateStart = dateStart;
    }

    public String getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(String dateEnd) {
        this.dateEnd = dateEnd;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public String toString() {
      return "Extension reason " + reason + " Additional text: " + additionalText + "  Date start: " + dateStart + "  Date end: " + dateEnd;
    }
}
