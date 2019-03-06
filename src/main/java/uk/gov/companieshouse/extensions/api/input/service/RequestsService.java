package uk.gov.companieshouse.extensions.api.input.service;

import java.time.LocalDate;
import java.time.Month;

import org.springframework.stereotype.Component;

import uk.gov.companieshouse.extensions.api.input.dto.ExtensionsRequest;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
public class RequestsService {

    private static final Logger LOGGER = LoggerFactory.getLogger("service.input.api.extensions.ch.gov.uk");

    public ExtensionsRequest getExtensionsRequestById(String id){
        ExtensionsRequest extensionsRequest =  new ExtensionsRequest();
        extensionsRequest.setUser("Joe Bloggs");
        extensionsRequest.setAccountingPeriodStartDate(LocalDate.of(2018, Month.APRIL, 1));
        extensionsRequest.setAccountingPeriodEndDate(LocalDate.of(2019, Month.MARCH, 31));
        LOGGER.info(extensionsRequest.toString());
        return extensionsRequest;
    }
}
