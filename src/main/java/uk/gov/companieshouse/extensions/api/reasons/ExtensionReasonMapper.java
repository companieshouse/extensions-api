package uk.gov.companieshouse.extensions.api.reasons;

import org.springframework.stereotype.Component;

@Component
public class ExtensionReasonMapper {
    public ExtensionReasonDTO entityToDTO(ExtensionReasonEntity entity) {
        ExtensionReasonDTO extensionReasonDTO = new ExtensionReasonDTO();
        extensionReasonDTO.setEtag(entity.getEtag());
        extensionReasonDTO.setAdditionalText(entity.getAdditionalText());
        extensionReasonDTO.setReasonInformation(entity.getReasonInformation());
        extensionReasonDTO.setContinuedIllness(entity.getContinuedIllness());
        extensionReasonDTO.setAffectedPerson(entity.getAffectedPerson());
        extensionReasonDTO.setStartOn(entity.getStartOn());
        extensionReasonDTO.setEndOn(entity.getEndOn());
        extensionReasonDTO.setReason(entity.getReason());
        extensionReasonDTO.setId(entity.getId());
        extensionReasonDTO.setLinks(entity.getLinks());

        // TODO map attachment links

        return extensionReasonDTO;
    }
}
