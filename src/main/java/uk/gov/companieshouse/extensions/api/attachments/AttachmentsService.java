package uk.gov.companieshouse.extensions.api.attachments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestFullEntity;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestsRepository;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.links.Links;

import java.util.Optional;
import java.util.UUID;

@Service
public class AttachmentsService {

    private ExtensionRequestsRepository requestsRepo;

    @Autowired
    public AttachmentsService(ExtensionRequestsRepository requestsRepo) {
        this.requestsRepo = requestsRepo;
    }

    public ServiceResult<AttachmentDTO>
            addAttachment(MultipartFile file,
                          String attachmentsUri, String requestId,
                          String reasonId) throws ServiceException {
        String randomUUID = UUID.randomUUID().toString();

        Attachment attachment = new Attachment();
        attachment.setId(randomUUID);
        attachment.setName(file.getName());
        attachment.setSize(file.getSize());
        attachment.setContentType(file.getContentType());

        Optional<ExtensionRequestFullEntity> extension = requestsRepo.findById(requestId);
        extension
            .map(ExtensionRequestFullEntity::getReasons)
            .filter(reasons -> !reasons.isEmpty())
            .orElseThrow(() ->
                new ServiceException(String.format("Attempting to add an attachment to request %s " +
                    "that contains no Extension Reason.", requestId)))
            .stream()
            .filter(reason -> reason.getId().equals(reasonId))
            .forEachOrdered(reason -> reason.addAttachment(attachment));

        String linkToSelf = attachmentsUri + "/" + randomUUID;
        Links links = new Links();
        links.setLink(() ->  "self", linkToSelf);
        attachment.setLinks(links);

        requestsRepo.save(extension.get());

        return ServiceResult.accepted(AttachmentDTO.builder()
            .withAttachment(attachment)
            .withFile(file)
            .withLinks(links)
            .build());
    }
}
