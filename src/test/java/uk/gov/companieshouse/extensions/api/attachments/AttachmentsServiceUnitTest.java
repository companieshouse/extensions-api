package uk.gov.companieshouse.extensions.api.attachments;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.companieshouse.extensions.api.Utils.Utils;
import uk.gov.companieshouse.extensions.api.reasons.ExtensionReasonEntity;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestFullEntity;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestsRepository;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.ServiceResultStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AttachmentsServiceUnitTest {

    private static final String REQUEST_ID = "123";
    private static final String REASON_ID = "1234";
    private static final String ACCESS_URL = "/dummyUrl";
    private static final String FILENAME = "testMultipart.txt";

    @Mock
    private ExtensionRequestsRepository repo;

    @Test
    public void canAddAnAttachment() throws Exception {
        ExtensionRequestFullEntity entity = new ExtensionRequestFullEntity();
        entity.setId(REQUEST_ID);
        ExtensionReasonEntity reasonEntity = new ExtensionReasonEntity();
        reasonEntity.setId(REASON_ID);
        reasonEntity.setReason("illness");
        entity.setReasons(Arrays.asList(reasonEntity));
        when(repo.findById(anyString())).thenReturn(Optional.of(entity));

        AttachmentsService service = new AttachmentsService(repo);

        ServiceResult<AttachmentDTO> result =
            service.addAttachment(Utils.mockMultipartFile(),
                ACCESS_URL, REQUEST_ID, REASON_ID);

        assertEquals(result.getData().getContentType(), "text/plain");
        assertNotNull(result.getData().getId());
        assertEquals(result.getData().getName(), FILENAME);
        assertEquals(ServiceResultStatus.ACCEPTED, result.getStatus());

        Optional<Attachment> entityAttachment = entity.getReasons()
            .stream()
            .flatMap(reason -> reason.getAttachments().stream())
            .findAny();
        assertTrue(entityAttachment.isPresent());
        String linkUrl = entityAttachment.get().getLinks().getLinks().get("self");
        String downloadUrl = entityAttachment.get().getLinks().getLinks().get("download");
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

        AttachmentsService service = new AttachmentsService(repo);

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

        AttachmentsService service = new AttachmentsService(repo);

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

        AttachmentsService service = new AttachmentsService(repo);

        try {
            service.addAttachment(Utils.mockMultipartFile(),
                ACCESS_URL, REQUEST_ID, REASON_ID);
            fail();
        } catch(ServiceException e) {
            assertEquals(String.format("No request found: %s", REQUEST_ID), e.getMessage());
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
        assertEquals(entity.getReasons().get(0).getAttachments().size(), 2);

        AttachmentsService service = new AttachmentsService(repo);
        when(repo.findById(entity.getId()))
            .thenReturn(Optional.of(entity));

        service.removeAttachment(entity.getId(),
            entity.getReasons().stream().findAny().get().getId(), "12345");

        assertFalse(entity.getReasons().isEmpty());
        entity.getReasons()
            .stream()
            .forEach(reason -> {
                assertFalse(reason.getAttachments().isEmpty());
                assertEquals(reason.getAttachments().size(), 1);
                assertEquals(reason.getAttachments().get(0).getId(), "123456");
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

        AttachmentsService service = new AttachmentsService(repo);
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

        AttachmentsService service = new AttachmentsService(repo);
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

    private void addAttachmentToReason(ExtensionReasonEntity reason, String attachmentId) {
        Attachment attachment = new Attachment();
        attachment.setSize(2L);
        attachment.setName("filename");
        attachment.setId(attachmentId);
        reason.addAttachment(attachment);
    }
}
