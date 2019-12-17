package uk.gov.companieshouse.extensions.api.requests;

import uk.gov.companieshouse.extensions.api.reasons.ExtensionReasonEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ExtensionRequestFullEntityBuilder {

    private LocalDateTime createdOn;

    private String companyNumber;

    private CreatedBy createdBy;

    private LocalDateTime accountingPeriodStartOn;

    private LocalDateTime accountingPeriodEndOn;

    private Status status;

    private final List<ExtensionReasonEntity> reasons = new ArrayList<>();

    public ExtensionRequestFullEntityBuilder withCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
        return this;
    }

    public ExtensionRequestFullEntityBuilder withCreatedOn(Supplier<LocalDateTime> localDateTimeSupplier) {
        this.createdOn = localDateTimeSupplier.get();
        return this;
    }

    public ExtensionRequestFullEntityBuilder withCreatedBy(CreatedBy createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public ExtensionRequestFullEntityBuilder withAccountingPeriodStartOn(LocalDateTime
                                                                             accountingPeriodStartDate) {
        this.accountingPeriodStartOn = accountingPeriodStartDate;
        return this;
    }

    public ExtensionRequestFullEntityBuilder withAccountingPeriodEndOn(LocalDateTime
                                                                           accountingPeriodStartDate) {
        this.accountingPeriodEndOn = accountingPeriodStartDate;
        return this;
    }

    public ExtensionRequestFullEntityBuilder withStatus() {
        this.status = Status.OPEN;
        return this;
    }

    public static ExtensionRequestFullEntityBuilder newInstance() {
        return new ExtensionRequestFullEntityBuilder();
    }

    public ExtensionRequestFullEntity build() {
        ExtensionRequestFullEntity extensionRequestFullEntity = new ExtensionRequestFullEntity();
        extensionRequestFullEntity.setCompanyNumber(this.companyNumber);
        extensionRequestFullEntity.setCreatedOn(this.createdOn);
        extensionRequestFullEntity.setCreatedBy(this.createdBy);
        extensionRequestFullEntity.setAccountingPeriodStartOn(this.accountingPeriodStartOn);
        extensionRequestFullEntity.setAccountingPeriodEndOn(this.accountingPeriodEndOn);
        extensionRequestFullEntity.setReasons(this.reasons);
        extensionRequestFullEntity.setStatus(this.status);

        return extensionRequestFullEntity;
    }
}
