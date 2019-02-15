package uk.gov.companieshouse.extensions.api.input.dto;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExtensionsRequest {

    private String user;
    @JsonProperty("accounting_period_start_date")
    private Date accountingPeriodStartDate;
    @JsonProperty("accounting_period_end_date")
    private Date accountingPeriodEndDate;
    private List<Reason> reasons;


    public String toString() {
      return "User " + user + " Acc period start: " + accountingPeriodStartDate + "  Acc period end: " + accountingPeriodEndDate;
    }
}
