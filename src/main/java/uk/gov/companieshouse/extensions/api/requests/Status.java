package uk.gov.companieshouse.extensions.api.requests;

public enum Status {
    /**
     * Status of a newly created extension request whose details are being populated.
     */
    OPEN,

    /**
     * An extension request that has been submitted for processing.
     */
    SUBMITTED,

    /**
     * Indicates that an extension request was automatically rejected as the accounts
     * filing date has already been extended to the maximum period possible.
     */
    REJECTED_MAX_EXT_LENGTH_EXCEEDED
}
