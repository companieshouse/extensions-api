package uk.gov.companieshouse.extensions.api.attachments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.extensions.api.reasons.ExtensionReasonEntity;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestFullEntity;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestsRepository;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.links.Links;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

        ExtensionRequestFullEntity extension = requestsRepo.findById(requestId)
            .orElseThrow(missingRequest(requestId));

        extension.mapToReason(reasonId)
            .orElseThrow(missingReason(requestId, reasonId))
            .addAttachment(attachment);

        String linkToSelf = attachmentsUri + "/" + randomUUID;
        Links links = new Links();
        links.setLink(() ->  "self", linkToSelf);
        links.setLink(() -> "download", linkToSelf + "/download");
        attachment.setLinks(links);

        requestsRepo.save(extension);

        return ServiceResult.accepted(AttachmentDTO.builder()
            .withAttachment(attachment)
            .withFile(file)
            .withLinks(links)
            .build());
    }

    public ServiceResult<Void> removeAttachment(String requestId,
            String reasonId, String attachmentId) throws ServiceException {
        ExtensionRequestFullEntity extension = requestsRepo.findById(requestId)
            .orElseThrow(missingRequest(requestId));

        ExtensionReasonEntity reason = extension.mapToReason(reasonId)
            .orElseThrow(missingReason(requestId, reasonId));

        if(reason.getAttachments().isEmpty()) {
            throw new ServiceException(String.format("Reason %s contains no attachment to delete: %s",
                reasonId, attachmentId));
        }

        List<Attachment> updatedAttachments =
            reason.getAttachments()
                .stream()
                .filter(attachment -> !attachment.getId().equals(attachmentId))
                .collect(Collectors.toList());

        if(updatedAttachments.size() == reason.getAttachments().size()) {
            throw new ServiceException(String.format("Attachment %s does not exist in reason %s",
                attachmentId, reasonId));
        }

        reason.setAttachments(updatedAttachments);

        requestsRepo.save(extension);
        return ServiceResult.deleted();
    }

    private Supplier<ServiceException> missingRequest(String requestId) {
        return () -> new ServiceException(String.format("No request found: %s", requestId));
    }

    private Supplier<ServiceException> missingReason(String requestId, String reasonId) {
        return () -> new ServiceException(String.format("Reason %s not found in " +
            "Request %s", reasonId, requestId));
    }
}
