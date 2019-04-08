package uk.gov.companieshouse.extensions.api.reasons;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import uk.gov.companieshouse.extensions.api.attachments.Attachment;

import java.util.List;

@Getter
@Setter
public class ExtensionReason {

    private String reason;

    @JsonProperty("additional_text")
    private String additionalText;

    @JsonProperty("date_start")
    private String dateStart;

    @JsonProperty("date_end")
    private String dateEnd;

    private List<Attachment> attachments;

    public String toString() {
      return "Extension reason " + reason + " Additional text: " + additionalText + "  Date start: " + dateStart + "  Date end: " + dateEnd;
    }
}
