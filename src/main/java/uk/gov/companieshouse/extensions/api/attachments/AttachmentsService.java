package uk.gov.companieshouse.extensions.api.attachments;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.extensions.api.attachments.upload.FileUploader;
import uk.gov.companieshouse.extensions.api.attachments.upload.FileUploaderResponse;
import uk.gov.companieshouse.extensions.api.reasons.ExtensionReasonEntity;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestFullEntity;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestsRepository;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.links.Links;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class AttachmentsService {

    private ExtensionRequestsRepository requestsRepo;

    private FileUploader fileUploader;

    @Autowired
    public AttachmentsService(ExtensionRequestsRepository requestsRepo, FileUploader fileUploader) {
        this.requestsRepo = requestsRepo;
        this.fileUploader = fileUploader;
    }

    public ServiceResult<AttachmentDTO>
            addAttachment(@NotNull MultipartFile file,
                          String attachmentsUri, String requestId,
                          String reasonId) throws ServiceException {

        FileUploaderResponse fileUploaderResponse = uploadFile(file);

        String attachmentId = fileUploaderResponse.getFileId();
        Attachment attachment = getAttachment(file, attachmentId);

        ExtensionRequestFullEntity extension = requestsRepo.findById(requestId)
            .orElseThrow(missingRequest(requestId));

        extension.mapToReason(reasonId)
            .orElseThrow(missingReason(requestId, reasonId))
            .addAttachment(attachment);

        Links links = getLinks(attachmentsUri, attachmentId);
        attachment.setLinks(links);

        requestsRepo.save(extension);

        return ServiceResult.accepted(AttachmentDTO.builder()
            .withAttachment(attachment)
            .withFile(file)
            .withLinks(links)
            .build());
    }

    private FileUploaderResponse uploadFile(@NotNull MultipartFile file) throws ServiceException {
        FileUploaderResponse fileUploaderResponse = fileUploader.upload(file);
        if (fileUploaderResponse.isInError()) {
            throw new ServiceException(fileUploaderResponse.getErrorMessage());
        }
        if (StringUtils.isBlank(fileUploaderResponse.getFileId())) {
            throw new ServiceException("No file id returned from file upload");
        }
        return fileUploaderResponse;
    }

    private Attachment getAttachment(@NotNull MultipartFile file, String attachmentId) {
        Attachment attachment = new Attachment();
        attachment.setId(attachmentId);
        String filename = file.getOriginalFilename();
        attachment.setName(filename);
        attachment.setSize(file.getSize());
        attachment.setContentType(file.getContentType());
        return attachment;
    }

    private Links getLinks(String attachmentsUri, String attachmentId) {
        String linkToSelf = attachmentsUri + "/" + attachmentId;
        Links links = new Links();
        links.setLink(() ->  "self", linkToSelf);
        links.setLink(() -> "download", linkToSelf + "/download");
        return links;
    }

    public ServiceResult<Void> removeAttachment(String requestId,
            String reasonId, String attachmentId) throws ServiceException {
        ExtensionRequestFullEntity extension = requestsRepo.findById(requestId)
            .orElseThrow(missingRequest(requestId));

        ExtensionReasonEntity reason = extension.mapToReason(reasonId)
            .orElseThrow(missingReason(requestId, reasonId));

        List<Attachment> reasonAttachments = reason.getAttachments();

        if (reasonAttachments.isEmpty()) {
            throw new ServiceException(String.format("Reason %s contains no attachment to delete: %s",
                reasonId, attachmentId));
        }

        List<Attachment> updatedAttachments =
            reasonAttachments
                .stream()
                .filter(attachment -> !attachment.getId().equals(attachmentId))
                .collect(Collectors.toList());

        if (updatedAttachments.size() == reasonAttachments.size()) {
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
