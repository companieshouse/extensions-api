package uk.gov.companieshouse.extensions.api.reasons;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.extensions.api.requests.ExtensionsLinkKeys;
import uk.gov.companieshouse.service.links.Links;

import java.util.HashMap;
import java.util.Map;

@Component
public class ExtensionReasonMapper {
    public ExtensionReasonDTO entityToDTO(ExtensionReasonEntity entity) {
        ExtensionReasonDTO extensionReasonDTO = new ExtensionReasonDTO();
        extensionReasonDTO.setEtag(entity.getEtag());
        extensionReasonDTO.setReasonInformation(entity.getReasonInformation());
        extensionReasonDTO.setContinuedIllness(entity.getContinuedIllness());
        extensionReasonDTO.setAffectedPerson(entity.getAffectedPerson());
        extensionReasonDTO.setStartOn(entity.getStartOn());
        extensionReasonDTO.setEndOn(entity.getEndOn());
        extensionReasonDTO.setReason(entity.getReason());
        extensionReasonDTO.setId(entity.getId());
        extensionReasonDTO.setLinks(entity.getLinks());
        extensionReasonDTO.setReasonStatus(entity.getReasonStatus());

        Links attachments = new Links();
        Map<String, String> linksMap = new HashMap<>();
        entity.getAttachments().forEach(
            attachment -> linksMap.put(attachment.getName(), attachment.getLinks().getLink(ExtensionsLinkKeys.SELF)));
        attachments.setLinks(linksMap);
        extensionReasonDTO.setAttachments(attachments);

        return extensionReasonDTO;
    }
}
