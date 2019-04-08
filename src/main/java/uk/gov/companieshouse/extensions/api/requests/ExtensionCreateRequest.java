package uk.gov.companieshouse.extensions.api.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ExtensionCreateRequest {

    private String user;

    @JsonProperty("accounting_period_start_date")
    private LocalDate accountingPeriodStartDate;

    @JsonProperty("accounting_period_end_date")
    private LocalDate accountingPeriodEndDate;
}
