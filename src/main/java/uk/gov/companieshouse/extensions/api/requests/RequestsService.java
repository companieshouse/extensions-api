package uk.gov.companieshouse.extensions.api.requests;

import java.time.LocalDate;
import java.time.Month;

import org.springframework.stereotype.Component;

@Component
public class RequestsService {

    public ExtensionRequest getExtensionsRequestById(String id){
        ExtensionRequest extensionRequest =  new ExtensionRequest();
        extensionRequest.setUser("Joe Bloggs");
        extensionRequest.setAccountingPeriodStartDate(LocalDate.of(2018, Month.APRIL, 1));
        extensionRequest.setAccountingPeriodEndDate(LocalDate.of(2019, Month.MARCH, 31));
        return extensionRequest;
    }

}
