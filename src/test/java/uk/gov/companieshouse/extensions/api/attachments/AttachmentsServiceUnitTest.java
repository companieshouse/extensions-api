package uk.gov.companieshouse.extensions.api.attachments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;

import uk.gov.companieshouse.extensions.api.Utils.Utils;
import uk.gov.companieshouse.extensions.api.attachments.file.FileTransferApiClient;
import uk.gov.companieshouse.extensions.api.attachments.file.FileTransferApiClientResponse;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;
import uk.gov.companieshouse.extensions.api.reasons.ExtensionReasonEntity;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestFullEntity;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestsRepository;
import uk.gov.companieshouse.extensions.api.requests.ExtensionsLinkKeys;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.ServiceResultStatus;

@Tag("Unit")
@ExtendWith(MockitoExtension.class)
public class AttachmentsServiceUnitTest {

    private static final String REQUEST_ID = "123";
    private static final String REASON_ID = "1234";
    private static final String ACCESS_URL = "/dummyUrl";
    private static final String UPLOAD_ID = "5agf-g6hh";
    private static final String NO_FILE_ID_MESSAGE = "No file id returned from file upload";

    @Mock
    private ExtensionRequestsRepository repo;

    @Mock
    private FileTransferApiClient fileTransferApiClient;

    @Mock
    private ApiLogger apiLogger;

    private AttachmentsService service;

    @BeforeEach
    public void setup() {
        service = new AttachmentsService(repo, fileTransferApiClient, apiLogger);
    }

    @Test
    public void canAddAnAttachment() throws Exception {
        ExtensionRequestFullEntity entity = new ExtensionRequestFullEntity();
        entity.setId(REQUEST_ID);
        ExtensionReasonEntity reasonEntity = new ExtensionReasonEntity();
        reasonEntity.setId(REASON_ID);
        reasonEntity.setReason("illness");
        entity.setReasons(Arrays.asList(reasonEntity));
        when(fileTransferApiClient.upload(any(MultipartFile.class))).thenReturn(getSuccessfulUploadResponse());
        when(repo.findById(anyString())).thenReturn(Optional.of(entity));


        ServiceResult<AttachmentDTO> result =
            service.addAttachment(Utils.mockMultipartFile(),
                ACCESS_URL, REQUEST_ID, REASON_ID);

        assertEquals("text/plain", result.getData().getContentType());
        assertNotNull(result.getData().getId());
        assertEquals(Utils.ORIGINAL_FILE_NAME, result.getData().getName());
        assertEquals(ServiceResultStatus.ACCEPTED, result.getStatus());

        Optional<Attachment> entityAttachment = entity.getReasons()
            .stream()
            .flatMap(reason -> reason.getAttachments().stream())
            .findAny();
        assertTrue(entityAttachment.isPresent());
        String linkUrl = entityAttachment.get().getLinks().getLink(ExtensionsLinkKeys.SELF);
        String downloadUrl = entityAttachment.get().getLinks().getLink(ExtensionsLinkKeys.DOWNLOAD);
        assertEquals(linkUrl + "/download", downloadUrl);
        assertTrue(linkUrl.startsWith(ACCESS_URL));
        assertFalse(linkUrl.endsWith(ACCESS_URL + "/"));
        assertNotNull(entityAttachment.get().getId());

        verify(repo).save(entity);
        verify(repo).findById(REQUEST_ID);
    }

    @Test
    public void willNotOverrideAlreadyExistingAttachments() throws Exception {
        ExtensionRequestFullEntity entity = new ExtensionRequestFullEntity();
        entity.setId(REQUEST_ID);
        ExtensionReasonEntity reasonEntity = new ExtensionReasonEntity();
        reasonEntity.setId(REASON_ID);
        reasonEntity.setReason("illness");
        Attachment attachment = new Attachment();
        attachment.setSize(1L);
        attachment.setContentType("text/plain");
        attachment.setName("testFile");
        attachment.setId("12345a");
        List<Attachment> attachmentsList = new ArrayList<>();
        attachmentsList.add(attachment);
        reasonEntity.setAttachments(attachmentsList);
        entity.setReasons(Arrays.asList(reasonEntity));
        when(fileTransferApiClient.upload(any(MultipartFile.class))).thenReturn(getSuccessfulUploadResponse());
        when(repo.findById(anyString())).thenReturn(Optional.of(entity));


        service.addAttachment(Utils.mockMultipartFile(),
            ACCESS_URL, REQUEST_ID, REASON_ID);

        List<Attachment> entityAttachments = entity.getReasons()
            .stream()
            .flatMap(reason -> reason.getAttachments().stream())
            .collect(Collectors.toList());

        assertEquals(2, entityAttachments.size());
        assertEquals("testFile", entityAttachments.get(0).getName());
        assertEquals(Utils.ORIGINAL_FILE_NAME, entityAttachments.get(1).getName());
    }

    @Test
    public void willThrowServiceExceptionIfAttachmentAddedWithNoReason() throws Exception {
        ExtensionRequestFullEntity entity = new ExtensionRequestFullEntity();
        entity.setId(REQUEST_ID);
        when(fileTransferApiClient.upload(any(MultipartFile.class))).thenReturn(getSuccessfulUploadResponse());
        when(repo.findById(anyString())).thenReturn(Optional.of(entity));

        try {
            service.addAttachment(Utils.mockMultipartFile(),
                ACCESS_URL, REQUEST_ID, REASON_ID);
            fail();
        } catch(ServiceException e) {
            assertEquals(String.format("Reason %s not found in " +
                "Request %s", REASON_ID, REQUEST_ID), e.getMessage());
        }
    }

    @Test
    public void willThrowServiceExceptionIfNoReason() throws Exception {
        when(fileTransferApiClient.upload(any(MultipartFile.class))).thenReturn(getSuccessfulUploadResponse());
        when(repo.findById(anyString())).thenReturn(Optional.empty());

        try {
            service.addAttachment(Utils.mockMultipartFile(),
                ACCESS_URL, REQUEST_ID, REASON_ID);
            fail();
        } catch(ServiceException e) {
            assertEquals(String.format("No request found with request id %s", REQUEST_ID), e.getMessage());
        }
    }

    @Test
    public void willThrowServiceExceptionIfUploadErrors() throws Exception {
        when(fileTransferApiClient.upload(any(MultipartFile.class))).thenReturn(getUnsuccessfulUploadResponse());

        try {
            service.addAttachment(Utils.mockMultipartFile(),
                ACCESS_URL, REQUEST_ID, REASON_ID);
            fail();
        } catch(ServiceException e) {
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
        }
    }

    @Test
    public void willPropagateServerRuntimeExceptions() throws Exception {
        when(fileTransferApiClient.upload(any(MultipartFile.class)))
            .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            service.addAttachment(Utils.mockMultipartFile(),
                ACCESS_URL, REQUEST_ID, REASON_ID);
            fail();
        } catch(HttpServerErrorException e) {
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
        }
    }

    @Test
    public void willPropagateClientRuntimeExceptions() throws Exception {
        when(fileTransferApiClient.upload(any(MultipartFile.class)))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        try {
            service.addAttachment(Utils.mockMultipartFile(),
                ACCESS_URL, REQUEST_ID, REASON_ID);
            fail();
        } catch(HttpClientErrorException e) {
            assertEquals(HttpStatus.BAD_REQUEST.toString(), e.getMessage());
        }
    }

    @Test
    public void willThrowServiceExceptionIfNoFileIdReturned() throws Exception {
        FileTransferApiClientResponse response = getSuccessfulUploadResponse();
        response.setFileId(null);
        when(fileTransferApiClient.upload(any(MultipartFile.class))).thenReturn(response);

        try {
            service.addAttachment(Utils.mockMultipartFile(),
                ACCESS_URL, REQUEST_ID, REASON_ID);
            fail();
        } catch(ServiceException e) {
            assertEquals(NO_FILE_ID_MESSAGE, e.getMessage());
        }
    }

    @Test
    public void willRemoveAttachmentFromReason() throws ServiceException {
        ExtensionRequestFullEntity entity = Utils.dummyRequestEntity();
        List<ExtensionReasonEntity> reasons = new ArrayList<>();
        reasons.add(Utils.dummyReasonEntity());
        entity.setReasons(reasons);
        entity.getReasons()
            .forEach(reason -> {
                addAttachmentToReason(reason, "12345");
                addAttachmentToReason(reason, "123456");
            });

        assertFalse(entity.getReasons().get(0).getAttachments().isEmpty());
        assertEquals(2, entity.getReasons().get(0).getAttachments().size());

        when(repo.findById(entity.getId()))
            .thenReturn(Optional.of(entity));

        FileTransferApiClientResponse apiClientResponse = new FileTransferApiClientResponse();
        apiClientResponse.setHttpStatus(HttpStatus.NO_CONTENT);
        when(fileTransferApiClient.delete("12345")).thenReturn(apiClientResponse);

        service.removeAttachment(entity.getId(),
            entity.getReasons().stream().findAny().get().getId(), "12345");

        assertFalse(entity.getReasons().isEmpty());
        entity.getReasons()
            .forEach(reason -> {
                assertFalse(reason.getAttachments().isEmpty());
                assertEquals(1, reason.getAttachments().size());
                assertEquals("123456", reason.getAttachments().get(0).getId());
            });

        verify(repo).save(entity);
        verify(fileTransferApiClient, times(1)).delete("12345");
        verify(fileTransferApiClient, never()).delete("123456");
        verify(apiLogger, never()).error(anyString(), any(Exception.class));
        verify(apiLogger, never()).error(anyString());
    }

    @Test
    public void willThrowExceptionIfNoAttachmentsToRemove() {
        ExtensionRequestFullEntity entity = Utils.dummyRequestEntity();
        List<ExtensionReasonEntity> reasons = new ArrayList<>();
        reasons.add(Utils.dummyReasonEntity());
        entity.setReasons(reasons);

        assertTrue(entity.getReasons().get(0).getAttachments().isEmpty());

        when(repo.findById(entity.getId()))
            .thenReturn(Optional.of(entity));

        try {
            service.removeAttachment(entity.getId(),
                entity.getReasons().stream().findAny().get().getId(), "12345");
            fail();
        } catch(ServiceException e) {
            assertEquals(String.format("Reason %s contains no attachment to delete: %s",
                entity.getReasons().stream().findAny().get().getId(), "12345"), e.getMessage());
        }

        verify(repo, never()).save(entity);
    }

    @Test
    public void willThrowExceptionIfAttachmentDoesNotExist() {
        ExtensionRequestFullEntity entity = Utils.dummyRequestEntity();
        List<ExtensionReasonEntity> reasons = new ArrayList<>();
        reasons.add(Utils.dummyReasonEntity());
        entity.setReasons(reasons);
        entity.getReasons()
            .forEach(reason -> {
                addAttachmentToReason(reason, "12345");
                addAttachmentToReason(reason, "123456");
            });

        assertFalse(entity.getReasons().get(0).getAttachments().isEmpty());

        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.NOT_FOUND);
        when(fileTransferApiClient.delete("12345ab")).thenThrow(exception);

        when(repo.findById(entity.getId()))
            .thenReturn(Optional.of(entity));

        try {
            service.removeAttachment(entity.getId(),
                entity.getReasons().stream().findAny().get().getId(), "12345ab");
            fail();
        } catch(ServiceException e) {
            assertEquals(String.format("Attachment %s does not exist in reason %s", "12345ab",
                entity.getReasons().stream().findAny().get().getId()), e.getMessage());
        }

        verify(repo, never()).save(entity);
        verify(fileTransferApiClient, times(1)).delete("12345ab");
        verify(apiLogger).error("Unable to delete attachment 12345ab, status code 404 NOT_FOUND", exception);
    }

    @Test
    public void willHandleClientExceptionOnDeleteAttachment() throws ServiceException {
        ExtensionRequestFullEntity entity = Utils.dummyRequestEntity();
        List<ExtensionReasonEntity> reasons = new ArrayList<>();
        reasons.add(Utils.dummyReasonEntity());
        entity.setReasons(reasons);
        entity.getReasons()
            .stream()
            .forEachOrdered(reason -> {
                addAttachmentToReason(reason, "12345");
                addAttachmentToReason(reason, "123456");
            });

        assertFalse(entity.getReasons().get(0).getAttachments().isEmpty());

        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.NOT_FOUND);
        when(fileTransferApiClient.delete("12345")).thenThrow(exception);

        when(repo.findById(entity.getId()))
            .thenReturn(Optional.of(entity));

        service.removeAttachment(entity.getId(),
                entity.getReasons().stream().findAny().get().getId(), "12345");

        verify(repo).save(entity);
        verify(fileTransferApiClient, times(1)).delete("12345");
        verify(apiLogger).error("Unable to delete attachment 12345, status code 404 NOT_FOUND", exception);
    }

    @Test
    public void willHandleServerExceptionOnDeleteAttachment() throws ServiceException {
        ExtensionRequestFullEntity entity = Utils.dummyRequestEntity();
        List<ExtensionReasonEntity> reasons = new ArrayList<>();
        reasons.add(Utils.dummyReasonEntity());
        entity.setReasons(reasons);
        entity.getReasons()
            .stream()
            .forEachOrdered(reason -> {
                addAttachmentToReason(reason, "12345");
                addAttachmentToReason(reason, "123456");
            });

        assertFalse(entity.getReasons().get(0).getAttachments().isEmpty());

        HttpServerErrorException exception = new HttpServerErrorException(HttpStatus.BAD_GATEWAY);
        when(fileTransferApiClient.delete("12345")).thenThrow(exception);

        when(repo.findById(entity.getId()))
            .thenReturn(Optional.of(entity));

        service.removeAttachment(entity.getId(),
            entity.getReasons().stream().findAny().get().getId(), "12345");

        verify(repo).save(entity);
        verify(fileTransferApiClient, times(1)).delete("12345");
        verify(apiLogger).error("Unable to delete attachment 12345, status code 502 BAD_GATEWAY", exception);
    }

    @Test
    public void willHandleNullApiResponseOnDeleteAttachment() throws ServiceException {
        ExtensionRequestFullEntity entity = Utils.dummyRequestEntity();
        List<ExtensionReasonEntity> reasons = new ArrayList<>();
        reasons.add(Utils.dummyReasonEntity());
        entity.setReasons(reasons);
        entity.getReasons()
            .stream()
            .forEachOrdered(reason -> {
                addAttachmentToReason(reason, "12345");
                addAttachmentToReason(reason, "123456");
            });

        assertFalse(entity.getReasons().get(0).getAttachments().isEmpty());

        when(fileTransferApiClient.delete("12345")).thenReturn(null);

        when(repo.findById(entity.getId()))
            .thenReturn(Optional.of(entity));

        service.removeAttachment(entity.getId(),
            entity.getReasons().stream().findAny().get().getId(), "12345");

        verify(repo).save(entity);
        verify(fileTransferApiClient, times(1)).delete("12345");
        verify(apiLogger).error("Unable to delete attachment 12345");
    }

    @Test
    public void willHandleNullHttpStatusApiResponseOnDeleteAttachment() throws ServiceException {
        ExtensionRequestFullEntity entity = Utils.dummyRequestEntity();
        List<ExtensionReasonEntity> reasons = new ArrayList<>();
        reasons.add(Utils.dummyReasonEntity());
        entity.setReasons(reasons);
        entity.getReasons()
            .stream()
            .forEachOrdered(reason -> {
                addAttachmentToReason(reason, "12345");
                addAttachmentToReason(reason, "123456");
            });

        assertFalse(entity.getReasons().get(0).getAttachments().isEmpty());

        FileTransferApiClientResponse response = new FileTransferApiClientResponse();
        response.setHttpStatus(null);
        when(fileTransferApiClient.delete("12345")).thenReturn(response);

        when(repo.findById(entity.getId()))
            .thenReturn(Optional.of(entity));

        service.removeAttachment(entity.getId(),
            entity.getReasons().stream().findAny().get().getId(), "12345");

        verify(repo).save(entity);
        verify(fileTransferApiClient, times(1)).delete("12345");
        verify(apiLogger).error("Unable to delete attachment 12345");
    }

    @Test
    public void willCallFileTransferGatewayForDownload() {
        String attachmentId = "1234";
        HttpServletResponse httpServletResponse = new MockHttpServletResponse();
        FileTransferApiClientResponse dummyDownloadResponse = Utils.dummyDownloadResponse();

        when(fileTransferApiClient.download(attachmentId, httpServletResponse)).thenReturn(dummyDownloadResponse);

        FileTransferApiClientResponse downloadServiceResult = service.downloadAttachment(attachmentId, httpServletResponse);

        verify(fileTransferApiClient, only()).download(attachmentId, httpServletResponse);
        verify(fileTransferApiClient, times(1)).download(attachmentId, httpServletResponse);

        assertNotNull(downloadServiceResult);
    }

    private void addAttachmentToReason(ExtensionReasonEntity reason, String attachmentId) {
        Attachment attachment = new Attachment();
        attachment.setSize(2L);
        attachment.setName("filename");
        attachment.setId(attachmentId);
        reason.addAttachment(attachment);
    }

    private FileTransferApiClientResponse getSuccessfulUploadResponse() {
        FileTransferApiClientResponse fileTransferApiClientResponse = new FileTransferApiClientResponse();
        fileTransferApiClientResponse.setFileId(UPLOAD_ID);
        return fileTransferApiClientResponse;
    }

    private FileTransferApiClientResponse getUnsuccessfulUploadResponse() {
        FileTransferApiClientResponse fileTransferApiClientResponse = new FileTransferApiClientResponse();
        fileTransferApiClientResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        return fileTransferApiClientResponse;
    }
}
