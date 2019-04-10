package uk.gov.companieshouse.extensions.api.requests;

import java.time.LocalDate;
import java.time.Month;

import org.springframework.stereotype.Component;

@Component
public class RequestsService {

    public ExtensionRequestFull getExtensionsRequestById(String id){
        ExtensionRequestFull extensionRequestFull =  new ExtensionRequestFull();
        extensionRequestFull.setAccountingPeriodStartOn(LocalDate.of(2018, Month.APRIL, 1));
        extensionRequestFull.setAccountingPeriodEndOn(LocalDate.of(2019, Month.MARCH, 31));
        return extensionRequestFull;
    }

}
