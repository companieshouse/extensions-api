package uk.gov.companieshouse.extensions.api.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class ExtensionCreateRequest {

    @JsonProperty("accounting_period_start_on")
    private LocalDate accountingPeriodStartOn;

    @JsonProperty("accounting_period_end_on")
    private LocalDate accountingPeriodEndOn;

    public LocalDate getAccountingPeriodStartOn() {
        return accountingPeriodStartOn;
    }

    public void setAccountingPeriodStartOn(LocalDate accountingPeriodStartDate) {
        this.accountingPeriodStartOn = accountingPeriodStartDate;
    }

    public LocalDate getAccountingPeriodEndOn() {
        return accountingPeriodEndOn;
    }

    public void setAccountingPeriodEndOn(LocalDate accountingPeriodEndDate) {
        this.accountingPeriodEndOn = accountingPeriodEndDate;
    }
}
