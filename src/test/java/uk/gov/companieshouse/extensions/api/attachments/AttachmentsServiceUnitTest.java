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
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.ServiceResultStatus;
import uk.gov.companieshouse.service.links.LinkKey;

import java.nio.file.Files;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AttachmentsServiceUnitTest {

    @Mock
    private ExtensionRequestsRepository repo;

    @Test
    public void canAddAnAttachment() throws Exception {
        final String requestID = "123";
        final String reasonId = "1234";
        final String accessUrl = "/dummyUrl";
        final String fileName = "testMultipart.txt";
        ExtensionRequestFullEntity entity = new ExtensionRequestFullEntity();
        entity.setId(requestID);
        ExtensionReasonEntity reasonEntity = new ExtensionReasonEntity();
        reasonEntity.setId(reasonId);
        reasonEntity.setReason("illness");
        entity.setReasons(Arrays.asList(reasonEntity));
        when(repo.findById(anyString())).thenReturn(Optional.of(entity));

        Resource rsc = new ClassPathResource("input/testMultipart.txt");

        AttachmentsService service = new AttachmentsService(repo);

        ServiceResult<AttachmentDTO> result =
            service.addAttachment(new MockMultipartFile(fileName,
                    fileName, "text/plain", Files.readAllBytes(rsc.getFile().toPath())),
                accessUrl, requestID, reasonId);

        assertEquals(result.getData().getContentType(), "text/plain");
        assertNotNull(result.getData().getId());
        assertEquals(result.getData().getName(), fileName);
        assertEquals(ServiceResultStatus.ACCEPTED, result.getStatus());

        Optional<Attachment> entityAttachment = entity.getReasons()
            .stream()
            .flatMap(reason -> reason.getAttachments().stream())
            .findAny();
        assertTrue(entityAttachment.isPresent());
        String linkUrl = entityAttachment.get().getLinks().getLinks().get("self");
        assertTrue(linkUrl.startsWith(accessUrl));
        assertFalse(linkUrl.endsWith(accessUrl + "/"));
        assertNotNull(entityAttachment.get().getId());

        verify(repo).save(entity);
        verify(repo).findById(requestID);
    }
}
