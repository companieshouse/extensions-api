package uk.gov.companieshouse.extensions.api.reasons;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;

@Getter
public class Reason {

    private String reason;
    @JsonProperty("additional_text")
    private String additionalText;
    @JsonProperty("date_start")
    private String dateStart;
    @JsonProperty("date_end")
    private String dateEnd;

    public String toString() {
      return "Reason " + reason + " Additional text: " + additionalText + "  Date start: " + dateStart + "  Date end: " + dateEnd;
    }
}
