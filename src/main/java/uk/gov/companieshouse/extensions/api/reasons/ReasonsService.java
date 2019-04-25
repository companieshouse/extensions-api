package uk.gov.companieshouse.extensions.api.reasons;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestFullEntity;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestsRepository;
import uk.gov.companieshouse.extensions.api.requests.RequestsService;
import uk.gov.companieshouse.service.links.Links;

import java.time.LocalDate;

@Component
public class ReasonsService {

    @Autowired
    private RequestsService requestsService;

    @Autowired
    private ExtensionRequestsRepository extensionRequestsRepository;

    public ExtensionRequestFullEntity addExtensionsReasonToRequest(ExtensionCreateReason extensionCreateReason, String requestId, String requestURI) {

        ExtensionRequestFullEntity extensionRequestFullEntity = requestsService.getExtensionsRequestById(requestId);

        ExtensionReasonEntityBuilder extensionReasonEntityBuilder = ExtensionReasonEntityBuilder.getInstance().withLinks(requestURI);

        String reason = extensionCreateReason.getReason();
        if (StringUtils.isNotBlank(reason)) {
            extensionReasonEntityBuilder.withReason(reason);
        }

        String additionalText = extensionCreateReason.getAdditionalText();
        if (StringUtils.isNotBlank(additionalText)) {
            extensionReasonEntityBuilder.withAdditionalText(additionalText);
        }

        LocalDate startOn = extensionCreateReason.getStartOn();
        if (startOn != null) {
            extensionReasonEntityBuilder.withStartOn(startOn);
        }

        LocalDate endOn = extensionCreateReason.getEndOn();
        if (endOn != null) {
            extensionReasonEntityBuilder.withEndOn(endOn);
        }

        ExtensionReasonEntity extensionReasonEntity = extensionReasonEntityBuilder.build();

        extensionRequestFullEntity.addReason(extensionReasonEntity);

        ExtensionRequestFullEntity extensionRequestFullEntityUpdated = extensionRequestsRepository.save(extensionRequestFullEntity);

        return extensionRequestFullEntityUpdated;
    }
}
