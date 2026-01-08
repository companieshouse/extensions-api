package uk.gov.companieshouse.extensions.api.attachments;

import static java.lang.String.format;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.extensions.api.attachments.file.FileTransferApiClientResponse;
import uk.gov.companieshouse.extensions.api.attachments.file.FileTransferServiceClient;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;
import uk.gov.companieshouse.extensions.api.logger.LogMethodCall;
import uk.gov.companieshouse.extensions.api.reasons.ExtensionReasonEntity;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestFullEntity;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestsRepository;
import uk.gov.companieshouse.extensions.api.requests.ExtensionsLinkKeys;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.links.Links;

@Service
public class AttachmentsService {

    private final ExtensionRequestsRepository requestsRepo;
    private final FileTransferServiceClient fileTransferServiceClient;
    private final ApiLogger apiLogger;

    @Autowired
    public AttachmentsService(ExtensionRequestsRepository requestsRepo,
                              FileTransferServiceClient fileTransferServiceClient,
                              ApiLogger logger) {
        this.requestsRepo = requestsRepo;
        this.fileTransferServiceClient = fileTransferServiceClient;
        this.apiLogger = logger;
    }

    @LogMethodCall
    public ServiceResult<AttachmentDTO> addAttachment(@NotNull MultipartFile file,
        String attachmentsUri, String requestId, String reasonId) throws ServiceException {
        apiLogger.info("test");

        String attachmentId = uploadFile(file);
        Attachment attachment = createAttachment(file, attachmentId);

        ExtensionRequestFullEntity extension = requestsRepo.findById(requestId)
            .orElseThrow(missingRequest(requestId));

        extension.mapToReason(reasonId)
            .orElseThrow(missingReason(requestId, reasonId))
            .addAttachment(attachment);

        Links links = createLinks(attachmentsUri, attachmentId);
        attachment.setLinks(links);

        requestsRepo.save(extension);

        return ServiceResult.accepted(AttachmentDTO.builder()
            .withAttachment(attachment)
            .withFile(file)
            .withLinks(links)
            .build());
    }

    private String uploadFile(@NotNull MultipartFile file) throws ServiceException {
        FileTransferApiClientResponse response = fileTransferServiceClient.upload(file);

        HttpStatus responseHttpStatus = response.getHttpStatus();
        if (responseHttpStatus != null && responseHttpStatus.isError()) {
            throw new ServiceException(responseHttpStatus.toString());
        }

        String fileId = response.getFileId();
        if (StringUtils.isBlank(fileId)) {
            throw new ServiceException("No file id returned from file upload");
        }

        return fileId;
    }

    private Attachment createAttachment(@NotNull MultipartFile file, String attachmentId) {
        Attachment attachment = new Attachment();
        attachment.setId(attachmentId);
        String filename = file.getOriginalFilename();
        attachment.setName(filename);
        attachment.setSize(file.getSize());
        attachment.setContentType(file.getContentType());
        return attachment;
    }

    private Links createLinks(String attachmentsUri, String attachmentId) {
        String linkToSelf = attachmentsUri + "/" + attachmentId;
        Links links = new Links();
        links.setLink(ExtensionsLinkKeys.SELF, linkToSelf);
        links.setLink(ExtensionsLinkKeys.DOWNLOAD, linkToSelf + "/download");
        return links;
    }

    @LogMethodCall
    public ServiceResult<Void> removeAttachment(String requestId, String reasonId, String attachmentId)
        throws ServiceException {

        ExtensionRequestFullEntity extension = requestsRepo.findById(requestId)
            .orElseThrow(missingRequest(requestId));

        ExtensionReasonEntity reason = extension.mapToReason(reasonId)
            .orElseThrow(missingReason(requestId, reasonId));

        List<Attachment> reasonAttachments = reason.getAttachments();
        if (reasonAttachments.isEmpty()) {
            throw new ServiceException(format("Reason %s contains no attachment to delete: %s", reasonId, attachmentId));
        }

        deleteAttachment(attachmentId);

        List<Attachment> updatedAttachments = reasonAttachments
                .stream()
                .filter(attachment -> !attachment.getId().equals(attachmentId))
                .collect(Collectors.toList());

        if (updatedAttachments.size() == reasonAttachments.size()) {
            throw new ServiceException(format("Attachment %s does not exist in reason %s", attachmentId, reasonId));
        }

        reason.setAttachments(updatedAttachments);

        requestsRepo.save(extension);

        return ServiceResult.deleted();
    }

    private void deleteAttachment(String attachmentId) {
        final String errorMessage = "Unable to delete attachment %s, status code %s";
        final String errorMessageShort = "Unable to delete attachment %s";

        try {
            FileTransferApiClientResponse response = fileTransferServiceClient.delete(attachmentId);
            if (response == null || response.getHttpStatus() == null) {
                apiLogger.error(format(errorMessageShort, attachmentId));
            } else {
                if (response.getHttpStatus().isError()) {
                    apiLogger.error(format(errorMessage, attachmentId, response.getHttpStatus()));
                }
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            apiLogger.error(format(errorMessage, attachmentId, e.getStatusCode()), e);
        }
    }

    private Supplier<ServiceException> missingRequest(String requestId) {
        return () -> new ServiceException(format("No request found with request id %s", requestId));
    }

    private Supplier<ServiceException> missingReason(String requestId, String reasonId) {
        return () -> new ServiceException(format("Reason %s not found in Request %s", reasonId, requestId));
    }

    public void downloadAttachment(String attachmentId, HttpServletResponse httpServletResponse) {
        fileTransferServiceClient.download(attachmentId, httpServletResponse);
    }
}
