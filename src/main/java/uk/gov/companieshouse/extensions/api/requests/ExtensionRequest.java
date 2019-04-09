package uk.gov.companieshouse.extensions.api.requests;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.data.mongodb.core.mapping.Document;
import uk.gov.companieshouse.extensions.api.reasons.ExtensionReason;
import uk.gov.companieshouse.service.links.Links;

@Document(collection = "extension_requests")
public class ExtensionRequest {

    private UUID id;

    private String user;

    private Links links;

    @JsonProperty("request_date")
    private LocalDateTime requestDate;

    private RequestStatus status;

    @JsonProperty("accounting_period_start_date")
    private LocalDate accountingPeriodStartDate;

    @JsonProperty("accounting_period_end_date")
    private LocalDate accountingPeriodEndDate;

    private List<ExtensionReason> reasons;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

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

    public List<ExtensionReason> getReasons() {
        return reasons;
    }

    public void setReasons(List<ExtensionReason> reasons) {
        this.reasons = reasons;
    }

    public String toString() {
      return "User " + user + " Acc period start: " + accountingPeriodStartDate + "  Acc period end: " + accountingPeriodEndDate;
    }
}
