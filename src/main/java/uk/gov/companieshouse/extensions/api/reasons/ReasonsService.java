package uk.gov.companieshouse.extensions.api.reasons;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.companieshouse.extensions.api.attachments.Attachment;
import uk.gov.companieshouse.extensions.api.attachments.file.FileTransferApiClient;
import uk.gov.companieshouse.extensions.api.attachments.file.FileTransferApiClientResponse;
import uk.gov.companieshouse.extensions.api.common.ExtensionDateUtils;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;
import uk.gov.companieshouse.extensions.api.logger.LogMethodCall;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestFullEntity;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestsRepository;
import uk.gov.companieshouse.extensions.api.requests.RequestsService;
import uk.gov.companieshouse.extensions.api.response.ListResponse;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.ServiceResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ReasonsService {

    private RequestsService requestsService;
    private ExtensionRequestsRepository extensionRequestsRepository;
    private ExtensionReasonMapper reasonMapper;
    private Supplier<String> randomUUid;
    private FileTransferApiClient fileTransferApiClient;
    private ApiLogger apiLogger;

    @Autowired
    public ReasonsService(RequestsService requestsService,
                          ExtensionRequestsRepository extensionRequestsRepository,
                          ExtensionReasonMapper reasonMapper,
                          Supplier<String> randomUUid,
                          FileTransferApiClient fileTransferApiClient,
                          ApiLogger apiLogger) {
        this.requestsService = requestsService;
        this.extensionRequestsRepository = extensionRequestsRepository;
        this.reasonMapper = reasonMapper;
        this.randomUUid = randomUUid;
        this.fileTransferApiClient = fileTransferApiClient;
        this.apiLogger = apiLogger;
    }

    @LogMethodCall
    public ServiceResult<ListResponse<ExtensionReasonDTO>> getReasons(String requestId) throws ServiceException {
        return requestsService.getExtensionsRequestById(requestId)
            .map(ExtensionRequestFullEntity::getReasons)
            .map(reasons -> reasons.stream()
                .map(reasonMapper::entityToDTO)
                .collect(Collectors.toList()))
            .map(reasonList -> ListResponse.<ExtensionReasonDTO>builder()
                .withItems(reasonList)
                .build())
            .map(ServiceResult::found)
            .orElseThrow(() ->
                new ServiceException(String.format("Extension request %s not found", requestId)));
    }

    @LogMethodCall
    public ServiceResult<ExtensionReasonDTO> addExtensionsReasonToRequest(ExtensionCreateReason extensionCreateReason,
                                          String requestId, String requestURI) throws ServiceException {

        ExtensionRequestFullEntity extensionRequestFullEntity = getRequest(requestId);
        String uuid = randomUUid.get();

        ExtensionReasonEntityBuilder extensionReasonEntityBuilder =
            ExtensionReasonEntityBuilder
                .builder()
                .withId(uuid)
                .withLinks(requestURI)
                .withReasonStatus(ReasonStatus.DRAFT);

        String reason = extensionCreateReason.getReason();
        if (StringUtils.isNotBlank(reason)) {
            extensionReasonEntityBuilder.withReason(reason);
        }

        String reasonInformation = extensionCreateReason.getReasonInformation();
        if (StringUtils.isNotBlank(reasonInformation)) {
            extensionReasonEntityBuilder.withReasonInformation(reasonInformation);
        }

        LocalDateTime startOn = extensionCreateReason.getStartOn();
        if (startOn != null) {
            extensionReasonEntityBuilder.withStartOn(ExtensionDateUtils.handleDSTOffsets(startOn));
        }

        LocalDateTime endOn = extensionCreateReason.getEndOn();
        if (endOn != null) {
            extensionReasonEntityBuilder.withEndOn(ExtensionDateUtils.handleDSTOffsets(endOn));
        }

        ExtensionReasonEntity extensionReasonEntity = extensionReasonEntityBuilder.build();

        extensionRequestFullEntity.addReason(extensionReasonEntity);

        ExtensionReasonEntity savedEntity = extensionRequestsRepository
            .save(extensionRequestFullEntity)
            .getReasons()
            .stream()
            .filter(extensionReason -> extensionReason.getId().equals(uuid))
            .findAny()
            .orElseThrow(() -> new ServiceException(String.format("Reason %s not saved in " +
                "database for request %s", uuid, requestId)));

        return ServiceResult.created(reasonMapper.entityToDTO(savedEntity));
    }

    @LogMethodCall
    public ExtensionRequestFullEntity removeExtensionsReasonFromRequest(String requestId, String
        reasonId) {

        ExtensionRequestFullEntity extensionRequestFullEntity = requestsService.getExtensionsRequestById(requestId)
            .orElse(null);

        if (extensionRequestFullEntity != null && !extensionRequestFullEntity.getReasons().isEmpty()) {
            deleteAttachments(reasonId, extensionRequestFullEntity);

            List<ExtensionReasonEntity> extensionRequestReasons = extensionRequestFullEntity
                .getReasons().stream().filter(reason -> !reason.getId().equals(reasonId)).collect(Collectors.toList());

            extensionRequestFullEntity.setReasons(extensionRequestReasons);

            return extensionRequestsRepository.save(extensionRequestFullEntity);
        }
        return extensionRequestFullEntity;
    }

    private void deleteAttachments(String reasonId, ExtensionRequestFullEntity extensionRequestFullEntity) {
        final String errorMessage = "Unable to delete attachment %s, status code %s";
        final String errorMessageShort = "Unable to delete attachment %s";

        Optional<ExtensionReasonEntity> reasonToBeDeleted = extensionRequestFullEntity.getReasons().stream()
            .filter(reason -> reason.getId().equals(reasonId))
            .findFirst();

        if (reasonToBeDeleted.isPresent()) {
            List<Attachment> attachmentsToBeDeleted = reasonToBeDeleted.get().getAttachments();
            for (Attachment attachment : attachmentsToBeDeleted) {
                try {
                    FileTransferApiClientResponse response = fileTransferApiClient.delete(attachment.getId());

                    if (response == null || response.getHttpStatus() == null) {
                        apiLogger.error(String.format(errorMessageShort,
                            attachment.getId()));
                    } else {
                        if (response.getHttpStatus().isError()) {
                            apiLogger.error(String.format(errorMessage,
                                attachment.getId(), response.getHttpStatus()));
                        }
                    }
                } catch (HttpClientErrorException | HttpServerErrorException e) {
                    apiLogger.error(String.format(errorMessage,
                        attachment.getId(), e.getStatusCode()), e);
                }
            }
        }
    }

    @LogMethodCall
    public ExtensionReasonDTO patchReason(ExtensionCreateReason createReason,
                                                         String requestId,
                                                         String reasonId) throws ServiceException {
        ExtensionRequestFullEntity extensionRequestFullEntity = getRequest(requestId);

        createReason.setStartOn(ExtensionDateUtils.handleDSTOffsets(createReason.getStartOn()));
        createReason.setEndOn(ExtensionDateUtils.handleDSTOffsets(createReason.getEndOn()));

        ExtensionReasonEntity reasonEntity =
            filterReasonToStream(extensionRequestFullEntity, reasonId)
                .findAny()
                .orElseThrow(() -> new ServiceException(String.format("Reason id %s not found in " +
                    "Request %s", reasonId, requestId)));

        final ExtensionReasonEntity newReason =
            PatchReasonMapper.INSTANCE.patchEntity(createReason, reasonEntity);

        filterReasonToStream(extensionRequestFullEntity, reasonId)
            .forEach(reason -> reason = newReason);

        extensionRequestsRepository.save(extensionRequestFullEntity);

        return reasonMapper.entityToDTO(newReason);
    }

    private Stream<ExtensionReasonEntity> filterReasonToStream(ExtensionRequestFullEntity fullEntity,
                                                     String reasonId) {
        return fullEntity.getReasons()
            .stream()
            .filter(reason -> reason.getId().equals(reasonId));
    }

    private ExtensionRequestFullEntity getRequest(String requestId) throws ServiceException {
        return requestsService.getExtensionsRequestById(requestId)
            .orElseThrow(() ->
                new ServiceException(String.format("Request %s not found", requestId)));
    }
}
