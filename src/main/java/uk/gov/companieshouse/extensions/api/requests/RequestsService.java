package uk.gov.companieshouse.extensions.api.requests;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.service.links.Links;

@Component
public class RequestsService {

    @Autowired
    private Supplier<LocalDateTime> dateTimeSupplierNow;

    @Autowired
    private ExtensionRequestsRepository extensionRequestsRepository;

    public ExtensionRequestFullEntity getExtensionsRequestById(String id){
        ExtensionRequestFullEntity extensionRequestFull =  new ExtensionRequestFullEntity();
        extensionRequestFull.setAccountingPeriodStartOn(LocalDate.of(2018, Month.APRIL, 1));
        extensionRequestFull.setAccountingPeriodEndOn(LocalDate.of(2019, Month.MARCH, 31));
        return extensionRequestFull;
    }

    public ExtensionRequestFullEntity insertExtensionsRequest(ExtensionCreateRequest extensionCreateRequest, CreatedBy
        createdBy, String reqUri) {

        ExtensionRequestFullEntity extensionRequestFullEntity = new ExtensionRequestFullEntity();
        extensionRequestFullEntity.setStatus(Status.OPEN);
        extensionRequestFullEntity.setReasons(new ArrayList<>());
        extensionRequestFullEntity.setCreatedOn(dateTimeSupplierNow.get());
        extensionRequestFullEntity.setAccountingPeriodStartOn(extensionCreateRequest.getAccountingPeriodStartOn());
        extensionRequestFullEntity.setAccountingPeriodEndOn(extensionCreateRequest.getAccountingPeriodEndOn());
        extensionRequestFullEntity.setCreatedBy(createdBy);

        ExtensionRequestFullEntity savedEntity = extensionRequestsRepository.insert
            (extensionRequestFullEntity);

        String linkToSelf = reqUri + savedEntity.getId();
        Links links = new Links();
        links.setLink(() ->  "self", linkToSelf);
        extensionRequestFullEntity.setLinks(links);
        return extensionRequestsRepository.save(extensionRequestFullEntity);
    }
}
