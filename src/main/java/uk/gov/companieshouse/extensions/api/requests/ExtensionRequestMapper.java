package uk.gov.companieshouse.extensions.api.requests;

import org.springframework.stereotype.Component;

@Component
public class ExtensionRequestMapper {
    public ExtensionRequestFullDTO entityToDTO(ExtensionRequestFullEntity entity) {

        ExtensionRequestFullDTO dto = new ExtensionRequestFullDTO();

        dto.setEtag(entity.getEtag());
        dto.setId(entity.getId());
        dto.setCreatedOn(entity.getCreatedOn());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setLinks(entity.getLinks());
        dto.setAccountingPeriodStartOn(entity.getAccountingPeriodStartOn());
        dto.setAccountingPeriodEndOn(entity.getAccountingPeriodEndOn());
        dto.setStatus(entity.getStatus());

        //TODO map request reason links

        return dto;
    }
}
