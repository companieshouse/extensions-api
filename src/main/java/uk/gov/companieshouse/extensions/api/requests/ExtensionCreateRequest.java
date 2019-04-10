package uk.gov.companieshouse.extensions.api.requests;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class ExtensionCreateRequest {

    @JsonProperty("accounting_period_start_date")
    private LocalDate accountingPeriodStartDate;

    @JsonProperty("accounting_period_end_date")
    private LocalDate accountingPeriodEndDate;

    public LocalDate getAccountingPeriodStartDate() {
        return accountingPeriodStartDate;
    }

    public void setAccountingPeriodStartDate(LocalDate accountingPeriodStartDate) {
        this.accountingPeriodStartDate = accountingPeriodStartDate;
    }

    public LocalDate getAccountingPeriodEndDate() {
        return accountingPeriodEndDate;
    }

    public void setAccountingPeriodEndDate(LocalDate accountingPeriodEndDate) {
        this.accountingPeriodEndDate = accountingPeriodEndDate;
    }
}
