package uk.gov.companieshouse.extensions.api.attachments;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import uk.gov.companieshouse.extensions.api.reasons.ExtensionReasonEntity;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestFullEntity;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestsRepository;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.ServiceResultStatus;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

        Resource rsc = new ClassPathResource("input/testMultipart.txt");

        AttachmentsService service = new AttachmentsService(repo);

        ServiceResult<AttachmentDTO> result =
            service.addAttachment(new MockMultipartFile(FILENAME,
                    FILENAME, "text/plain", Files.readAllBytes(rsc.getFile().toPath())),
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

        Resource rsc = new ClassPathResource("input/testMultipart.txt");

        AttachmentsService service = new AttachmentsService(repo);

        service.addAttachment(new MockMultipartFile(FILENAME,
                FILENAME, "text/plain", Files.readAllBytes(rsc.getFile().toPath())),
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

        Resource rsc = new ClassPathResource("input/testMultipart.txt");

        AttachmentsService service = new AttachmentsService(repo);

        try {
            service.addAttachment(new MockMultipartFile(FILENAME,
                    FILENAME, "text/plain", Files.readAllBytes(rsc.getFile().toPath())),
                ACCESS_URL, REQUEST_ID, REASON_ID);
            fail();
        } catch(ServiceException e) {
            assertEquals("Attempting to add an attachment to request " + REQUEST_ID +
                " that contains no Extension Reason.", e.getMessage());
        }
    }
}
