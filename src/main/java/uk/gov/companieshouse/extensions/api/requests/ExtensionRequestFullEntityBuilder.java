package uk.gov.companieshouse.extensions.api.requests;

import uk.gov.companieshouse.extensions.api.reasons.ExtensionReason;
import uk.gov.companieshouse.service.links.Links;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ExtensionRequestFullEntityBuilder {

    private String etag;

    private UUID id;

    private LocalDateTime createdOn;

    private CreatedBy createdBy;

    private LocalDate accountingPeriodStartOn;

    private LocalDate accountingPeriodEndOn;

    private Links links;

    private Status status;

    private List<ExtensionReason> reasons = new ArrayList<>();

    private ExtensionRequestFullEntityBuilder() {}

    public static ExtensionRequestFullEntityBuilder newInstance() {
        return new ExtensionRequestFullEntityBuilder();
    }



}
