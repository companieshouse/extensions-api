package uk.gov.companieshouse.extensions.api.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class ExtensionCreateRequest {

    @JsonProperty("accounting_period_start_on")
    private LocalDateTime accountingPeriodStartOn;

    @JsonProperty("accounting_period_end_on")
    private LocalDateTime accountingPeriodEndOn;

    public LocalDateTime getAccountingPeriodStartOn() {
        return accountingPeriodStartOn;
    }

    public void setAccountingPeriodStartOn(LocalDateTime accountingPeriodStartDate) {
        this.accountingPeriodStartOn = accountingPeriodStartDate;
    }

    public LocalDateTime getAccountingPeriodEndOn() {
        return accountingPeriodEndOn;
    }

    public void setAccountingPeriodEndOn(LocalDateTime accountingPeriodEndDate) {
        this.accountingPeriodEndOn = accountingPeriodEndDate;
    }
}
