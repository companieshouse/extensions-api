package uk.gov.companieshouse.extensions.api.requests;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.companieshouse.extensions.api.reasons.ExtensionReason;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ExtensionRequestFullEntityBuilder {

    @Autowired
    private static Supplier<LocalDateTime> dateTimeSupplierNow;

    private LocalDateTime createdOn;

    private CreatedBy createdBy;

    private LocalDate accountingPeriodStartOn;

    private LocalDate accountingPeriodEndOn;

    private Status status;

    private List<ExtensionReason> reasons = new ArrayList<>();

    public ExtensionRequestFullEntityBuilder withCreatedOn() {
        this.createdOn = dateTimeSupplierNow.get();
        return this;
    }

    public ExtensionRequestFullEntityBuilder withCreatedBy(CreatedBy createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public ExtensionRequestFullEntityBuilder withAccountingPeriodStartOn(LocalDate
                                                                             accountingPeriodStartDate) {
        this.accountingPeriodStartOn = accountingPeriodStartDate;
        return this;
    }

    public ExtensionRequestFullEntityBuilder withAccountingPeriodEndOn(LocalDate
                                                                           accountingPeriodStartDate) {
        this.accountingPeriodEndOn = accountingPeriodStartDate;
        return this;
    }

    public ExtensionRequestFullEntityBuilder withStatus() {
        this.status = Status.OPEN;
        return this;
    }

    public static ExtensionRequestFullEntityBuilder newInstance() {
        ExtensionRequestFullEntityBuilder extensionRequestFullEntityBuilder = new
            ExtensionRequestFullEntityBuilder();

        return new ExtensionRequestFullEntityBuilder();
    }

    public ExtensionRequestFullEntity build() {
        ExtensionRequestFullEntity extensionRequestFullEntity = new ExtensionRequestFullEntity();
        extensionRequestFullEntity.setCreatedOn(this.createdOn);
        extensionRequestFullEntity.setCreatedBy(this.createdBy);
        extensionRequestFullEntity.setAccountingPeriodStartOn(this.accountingPeriodStartOn);
        extensionRequestFullEntity.setAccountingPeriodEndOn(this.accountingPeriodEndOn);
        extensionRequestFullEntity.setReasons(this.reasons);
        extensionRequestFullEntity.setStatus(this.status);

        return extensionRequestFullEntity;
    }
}
