package uk.gov.companieshouse.extensions.api.attachments;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.multipart.MultipartFile;

import uk.gov.companieshouse.extensions.api.Utils.Utils;
import uk.gov.companieshouse.extensions.api.attachments.upload.FileTransferGateway;
import uk.gov.companieshouse.extensions.api.attachments.upload.FileTransferGatewayResponse;
import uk.gov.companieshouse.extensions.api.groups.Unit;
import uk.gov.companieshouse.extensions.api.reasons.ExtensionReasonEntity;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestFullEntity;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestsRepository;
import uk.gov.companieshouse.extensions.api.requests.ExtensionsLinkKeys;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.ServiceResultStatus;

@Category(Unit.class)
@RunWith(MockitoJUnitRunner.class)
public class AttachmentsServiceUnitTest {

    private static final String REQUEST_ID = "123";
    private static final String REASON_ID = "1234";
    private static final String ACCESS_URL = "/dummyUrl";
    private static final String FILENAME = "testMultipart.txt";
    private static final String UPLOAD_ID = "sjhkjsdfhkdshf";
    private static final String UPLOAD_FAILURE_MESSAGE = "Failure";
    private static final String NO_FILE_ID_MESSAGE = "No file id returned from file upload";

    @Mock
    private ExtensionRequestsRepository repo;

    @Mock
    private FileTransferGateway fileTransferGateway;

    @Before
    public void setup() {
        when(fileTransferGateway.upload(any(MultipartFile.class))).thenReturn(getSuccessfulUploadResponse());
    }

    @Test
    public void canAddAnAttachment() throws Exception {
        ExtensionRequestFullEntity entity = new ExtensionRequestFullEntity();
        entity.setId(REQUEST_ID);
        ExtensionReasonEntity reasonEntity = new ExtensionReasonEntity();
        reasonEntity.setId(REASON_ID);
        reasonEntity.setReason("illness");
        entity.setReasons(Arrays.asList(reasonEntity));
        when(repo.findById(anyString())).thenReturn(Optional.of(entity));

        AttachmentsService service = new AttachmentsService(repo, fileTransferGateway);

        ServiceResult<AttachmentDTO> result =
            service.addAttachment(Utils.mockMultipartFile(),
                ACCESS_URL, REQUEST_ID, REASON_ID);

        assertEquals("text/plain", result.getData().getContentType());
        assertNotNull(result.getData().getId());
        assertEquals(FILENAME, result.getData().getName());
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
        when(repo.findById(anyString())).thenReturn(Optional.of(entity));

        AttachmentsService service = new AttachmentsService(repo, fileTransferGateway);

        service.addAttachment(Utils.mockMultipartFile(),
            ACCESS_URL, REQUEST_ID, REASON_ID);

        List<Attachment> entityAttachments = entity.getReasons()
            .stream()
            .flatMap(reason -> reason.getAttachments().stream())
            .collect(Collectors.toList());

        assertEquals(2, entityAttachments.size());
        assertEquals("testFile", entityAttachments.get(0).getName());
        assertEquals(FILENAME, entityAttachments.get(1).getName());
    }

    @Test
    public void willThrowServiceExceptionIfAttachmentAddedWithNoReason() throws Exception {
        ExtensionRequestFullEntity entity = new ExtensionRequestFullEntity();
        entity.setId(REQUEST_ID);
        when(repo.findById(anyString())).thenReturn(Optional.of(entity));

        AttachmentsService service = new AttachmentsService(repo, fileTransferGateway);

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
        when(repo.findById(anyString())).thenReturn(Optional.ofNullable(null));

        AttachmentsService service = new AttachmentsService(repo, fileTransferGateway);

        try {
            service.addAttachment(Utils.mockMultipartFile(),
                ACCESS_URL, REQUEST_ID, REASON_ID);
            fail();
        } catch(ServiceException e) {
            assertEquals(String.format("No request found with request id %s", REQUEST_ID), e.getMessage());
        }
    }

    @Test
    public void willThrowServiceExceptionIfUploadFails() throws Exception {
        when(fileTransferGateway.upload(any(MultipartFile.class))).thenReturn(getUnsuccessfullUploadResponse());

        AttachmentsService service = new AttachmentsService(repo, fileTransferGateway);

        try {
            service.addAttachment(Utils.mockMultipartFile(),
                ACCESS_URL, REQUEST_ID, REASON_ID);
            fail();
        } catch(ServiceException e) {
            assertEquals(UPLOAD_FAILURE_MESSAGE, e.getMessage());
        }
    }

    @Test
    public void willThrowServiceExceptionIfNoFileIdReturned() throws Exception {
        FileTransferGatewayResponse response = getSuccessfulUploadResponse();
        response.setFileId(null);
        when(fileTransferGateway.upload(any(MultipartFile.class))).thenReturn(response);

        AttachmentsService service = new AttachmentsService(repo, fileTransferGateway);

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
            .stream()
            .forEachOrdered(reason -> {
                addAttachmentToReason(reason, "12345");
                addAttachmentToReason(reason, "123456");
            });

        assertFalse(entity.getReasons().get(0).getAttachments().isEmpty());
        assertEquals(2, entity.getReasons().get(0).getAttachments().size());

        AttachmentsService service = new AttachmentsService(repo, fileTransferGateway);
        when(repo.findById(entity.getId()))
            .thenReturn(Optional.of(entity));

        service.removeAttachment(entity.getId(),
            entity.getReasons().stream().findAny().get().getId(), "12345");

        assertFalse(entity.getReasons().isEmpty());
        entity.getReasons()
            .stream()
            .forEach(reason -> {
                assertFalse(reason.getAttachments().isEmpty());
                assertEquals(1, reason.getAttachments().size());
                assertEquals("123456", reason.getAttachments().get(0).getId());
            });

        verify(repo).save(entity);
    }

    @Test
    public void willThrowExceptionIfNoAttachmentsToRemove() {
        ExtensionRequestFullEntity entity = Utils.dummyRequestEntity();
        List<ExtensionReasonEntity> reasons = new ArrayList<>();
        reasons.add(Utils.dummyReasonEntity());
        entity.setReasons(reasons);

        assertTrue(entity.getReasons().get(0).getAttachments().isEmpty());

        AttachmentsService service = new AttachmentsService(repo, fileTransferGateway);
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
    public void willThrowExceptionIfAttachmentDoesntExist() {
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

        AttachmentsService service = new AttachmentsService(repo, fileTransferGateway);
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
    }

    @Test
    public void willCallFileTransferGatewayForDownload() {
        String attachmentId = "1234";
        OutputStream outputStream = new ByteArrayOutputStream();

        AttachmentsService service = new AttachmentsService(repo, fileTransferGateway);
        service.downloadAttachment(attachmentId, outputStream);

        verify(fileTransferGateway, only()).download(attachmentId, outputStream);
        verify(fileTransferGateway, times(1)).download(attachmentId, outputStream);
    }

    private void addAttachmentToReason(ExtensionReasonEntity reason, String attachmentId) {
        Attachment attachment = new Attachment();
        attachment.setSize(2L);
        attachment.setName("filename");
        attachment.setId(attachmentId);
        reason.addAttachment(attachment);
    }

    private FileTransferGatewayResponse getSuccessfulUploadResponse() {
        FileTransferGatewayResponse fileTransferGatewayResponse = new FileTransferGatewayResponse();
        fileTransferGatewayResponse.setInError(false);
        fileTransferGatewayResponse.setFileId(UPLOAD_ID);
        return fileTransferGatewayResponse;
    }

    private FileTransferGatewayResponse getUnsuccessfullUploadResponse() {
        FileTransferGatewayResponse fileTransferGatewayResponse = new FileTransferGatewayResponse();
        fileTransferGatewayResponse.setInError(true);
        fileTransferGatewayResponse.setErrorMessage(UPLOAD_FAILURE_MESSAGE);
        fileTransferGatewayResponse.setErrorStatusCode("500");
        fileTransferGatewayResponse.setErrorStatusText("Some failure");
        return fileTransferGatewayResponse;
    }
}
