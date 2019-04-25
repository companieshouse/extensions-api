package uk.gov.companieshouse.extensions.api.requests;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;
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

    public ExtensionRequestFullEntity getExtensionsRequestById(String id) {
        return extensionRequestsRepository.findById(id).orElse(null);
    }

    public ExtensionRequestFullEntity insertExtensionsRequest(ExtensionCreateRequest extensionCreateRequest, CreatedBy
        createdBy, String reqUri) {

        ExtensionRequestFullEntity extensionRequestFullEntity = ExtensionRequestFullEntityBuilder
            .newInstance()
            .withCreatedOn(dateTimeSupplierNow)
            .withCreatedBy(createdBy)
            .withAccountingPeriodStartOn(extensionCreateRequest.getAccountingPeriodStartOn())
            .withAccountingPeriodEndOn(extensionCreateRequest.getAccountingPeriodEndOn())
            .withStatus()
            .build();

        ExtensionRequestFullEntity savedEntity = extensionRequestsRepository.insert
            (extensionRequestFullEntity);

        String linkToSelf = reqUri + savedEntity.getId();
        Links links = new Links();
        links.setLink(() ->  "self", linkToSelf);
        extensionRequestFullEntity.setLinks(links);
        return extensionRequestsRepository.save(extensionRequestFullEntity);
    }
}
