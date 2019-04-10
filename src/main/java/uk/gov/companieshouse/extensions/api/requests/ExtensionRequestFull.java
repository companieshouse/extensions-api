package uk.gov.companieshouse.extensions.api.requests;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.data.mongodb.core.mapping.Document;
import uk.gov.companieshouse.service.links.Links;

@Document(collection = "extension_requests")
public class ExtensionRequestFull {

    private String etag;

    private UUID id;

    @JsonProperty("created_on")
    private LocalDateTime createdOn;

    @JsonProperty("created_by")
    private CreatedBy createdBy;

    // key = hash of reason id, value = uri of reason
    private Links reasons;

    @JsonProperty("accounting_period_start_on")
    private LocalDate accountingPeriodStartOn;

    @JsonProperty("accounting_period_end_on")
    private LocalDate accountingPeriodEndOn;

    private Links links;

    private Status status;

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public Links getReasons() {
        return reasons;
    }

    public void setReasons(Links reasons) {
        this.reasons = reasons;
    }

    public String toString() {
      return "Acc period start: " + accountingPeriodStartOn + "  Acc period end: " + accountingPeriodEndOn;
    }
}
