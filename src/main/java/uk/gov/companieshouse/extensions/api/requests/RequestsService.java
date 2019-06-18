package uk.gov.companieshouse.extensions.api.requests;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.extensions.api.logger.LogMethodCall;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.links.Links;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Component
public class RequestsService {

    @Autowired
    private Supplier<LocalDateTime> dateTimeSupplierNow;

    @Autowired
    private ExtensionRequestsRepository extensionRequestsRepository;

    @LogMethodCall
    public Optional<ExtensionRequestFullEntity> getExtensionsRequestById(String id) {
        return extensionRequestsRepository.findById(id);
    }

    @LogMethodCall
    public List<ExtensionRequestFullEntity> getExtensionsRequestListByCompanyNumber(String companyNumber) {
        return extensionRequestsRepository.findAllByCompanyNumber(companyNumber, Sort.by("_id").descending());
    }

    @LogMethodCall
    public ExtensionRequestFullEntity patchRequest(String requestId, RequestStatus status) throws ServiceException {
        ExtensionRequestFullEntity entity = extensionRequestsRepository.findById(requestId)
                .orElseThrow(() -> new ServiceException(String.format("Request: %s cannot be found", requestId)));

        entity = PatchRequestMapper.INSTANCE.patchEntity(status, entity);
        return extensionRequestsRepository.save(entity);
    }

    public ExtensionRequestFullEntity insertExtensionsRequest(ExtensionCreateRequest extensionCreateRequest, CreatedBy
        createdBy, String reqUri, String companyNumber) {

        ExtensionRequestFullEntity extensionRequestFullEntity = ExtensionRequestFullEntityBuilder
            .newInstance()
            .withCompanyNumber(companyNumber)
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
        links.setLink(ExtensionsLinkKeys.SELF, linkToSelf);
        extensionRequestFullEntity.setLinks(links);
        return extensionRequestsRepository.save(extensionRequestFullEntity);
    }
}
