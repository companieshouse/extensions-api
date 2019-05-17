package uk.gov.companieshouse.extensions.api.requests;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import uk.gov.companieshouse.service.links.Links;

public abstract class ExtensionRequestFull {

    private String etag;

    private String id;

    private String companyNumber;

    @JsonProperty("created_on")
    private LocalDateTime createdOn;

    @JsonProperty("created_by")
    private CreatedBy createdBy;

    @JsonProperty("accounting_period_start_on")
    private LocalDate accountingPeriodStartOn;

    @JsonProperty("accounting_period_end_on")
    private LocalDate accountingPeriodEndOn;

    @JsonUnwrapped
    private Links links;

    private Status status;

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public CreatedBy getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(CreatedBy createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDate getAccountingPeriodStartOn() {
        return accountingPeriodStartOn;
    }

    public void setAccountingPeriodStartOn(LocalDate accountingPeriodStartOn) {
        this.accountingPeriodStartOn = accountingPeriodStartOn;
    }

    public LocalDate getAccountingPeriodEndOn() {
        return accountingPeriodEndOn;
    }

    public void setAccountingPeriodEndOn(LocalDate accountingPeriodEndOn) {
        this.accountingPeriodEndOn = accountingPeriodEndOn;
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String toString() {
      return "id " + id + " Acc period start: " + accountingPeriodStartOn + "  Acc period end: " + accountingPeriodEndOn;
    }
}
