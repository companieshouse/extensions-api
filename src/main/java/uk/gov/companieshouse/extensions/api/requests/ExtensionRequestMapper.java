package uk.gov.companieshouse.extensions.api.requests;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.extensions.api.logger.LogMethodCall;

@Component
public class ExtensionRequestMapper {

    @LogMethodCall
    public ExtensionRequestFullDTO entityToDTO(ExtensionRequestFullEntity entity) {

        ExtensionRequestFullDTO dto = new ExtensionRequestFullDTO();

        dto.setEtag(entity.getEtag());
        dto.setId(entity.getId());
        dto.setCompanyNumber(entity.getCompanyNumber());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setLinks(entity.getLinks());
        dto.setAccountingPeriodStartOn(entity.getAccountingPeriodStartOn());
        dto.setAccountingPeriodEndOn(entity.getAccountingPeriodEndOn());
        dto.setStatus(entity.getStatus());

        entity.getReasons().forEach(
            reason -> dto.addReason(reason.getLinks())
        );

        return dto;
    }
}
