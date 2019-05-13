package uk.gov.companieshouse.extensions.api.requests;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.service.links.Links;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

@Component
public class RequestsService {

    @Autowired
    private Supplier<LocalDateTime> dateTimeSupplierNow;

    @Autowired
    private ExtensionRequestsRepository extensionRequestsRepository;

    public Optional<ExtensionRequestFullEntity> getExtensionsRequestById(String id) {
        return extensionRequestsRepository.findById(id);
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
