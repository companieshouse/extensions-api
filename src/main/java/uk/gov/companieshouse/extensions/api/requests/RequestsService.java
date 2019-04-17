package uk.gov.companieshouse.extensions.api.requests;

import java.time.LocalDate;
import java.time.Month;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RequestsService {

    @Autowired
    private ExtensionRequestsRepository extensionRequestsRepository;

    public ExtensionRequestFull getExtensionsRequestById(String id){
        ExtensionRequestFull extensionRequestFull =  new ExtensionRequestFull();
        extensionRequestFull.setAccountingPeriodStartOn(LocalDate.of(2018, Month.APRIL, 1));
        extensionRequestFull.setAccountingPeriodEndOn(LocalDate.of(2019, Month.MARCH, 31));
        return extensionRequestFull;
    }

    public void insertExtensionsRequest(ExtensionRequestFull extensionRequestFull) {
        extensionRequestsRepository.insert(extensionRequestFull);
    }

}
