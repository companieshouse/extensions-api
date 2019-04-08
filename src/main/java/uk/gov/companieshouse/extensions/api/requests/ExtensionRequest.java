package uk.gov.companieshouse.extensions.api.requests;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import uk.gov.companieshouse.extensions.api.attachments.Attachment;
import uk.gov.companieshouse.extensions.api.reasons.ExtensionReason;
import uk.gov.companieshouse.service.links.Links;

@Getter
@Setter
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

    private List<Attachment> attachments;


    public String toString() {
      return "User " + user + " Acc period start: " + accountingPeriodStartDate + "  Acc period end: " + accountingPeriodEndDate;
    }
}
