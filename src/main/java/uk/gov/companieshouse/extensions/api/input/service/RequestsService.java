package uk.gov.companieshouse.extensions.api.input.service;

import java.time.LocalDate;
import java.time.Month;

import org.springframework.stereotype.Component;

import uk.gov.companieshouse.extensions.api.input.dto.ExtensionsRequest;

@Component
public class RequestsService {

    public ExtensionsRequest getExtensionsRequestById(String id){
        ExtensionsRequest extensionsRequest =  new ExtensionsRequest();
        extensionsRequest.setUser("Joe Bloggs");     
        extensionsRequest.setAccountingPeriodStartDate(LocalDate.of(2018, Month.APRIL, 1));     
        extensionsRequest.setAccountingPeriodEndDate(LocalDate.of(2019, Month.MARCH, 31));
        return extensionsRequest;	
    }

}
