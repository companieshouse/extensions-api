package uk.gov.companieshouse.extensions.api.requests;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.gov.companieshouse.extensions.api.reasons.Reason;

@Getter
@Setter
@Document(collection = "extension_requests")
public class ExtensionRequest {

    private String user;

    @JsonProperty("accounting_period_start_date")
    private LocalDate accountingPeriodStartDate;

    @JsonProperty("accounting_period_end_date")
    private LocalDate accountingPeriodEndDate;

    private List<Reason> reasons;


    public String toString() {
      return "User " + user + " Acc period start: " + accountingPeriodStartDate + "  Acc period end: " + accountingPeriodEndDate;
    }
}
