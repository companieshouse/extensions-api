package uk.gov.companieshouse.extensions.api.requests;

import java.time.LocalDate;
import java.time.Month;

import org.springframework.stereotype.Component;

@Component
public class RequestsService {

    public ExtensionRequest getExtensionsRequestById(String id){
        return ExtensionRequest.builder()
            .user("Joe Bloggs")
            .accountingPeriodStartDate(LocalDate.of(2018, Month.APRIL, 1))
            .accountingPeriodEndDate(LocalDate.of(2019, Month.MARCH, 31))
            .build();
    }

}
