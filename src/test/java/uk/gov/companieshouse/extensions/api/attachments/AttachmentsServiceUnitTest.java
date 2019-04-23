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

import java.nio.file.Files;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
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
        ExtensionRequestFullEntity entity = new ExtensionRequestFullEntity();
        entity.setId(requestID);
        ExtensionReasonEntity reasonEntity = new ExtensionReasonEntity();
        reasonEntity.setId(reasonId);
        reasonEntity.setReason("illness");
        entity.setReasons(Arrays.asList(reasonEntity));
        when(repo.findById(anyString())).thenReturn(Optional.of(entity));

        Resource rsc = new ClassPathResource("input/testMultipart.txt");

        AttachmentsService service = new AttachmentsService(repo);

        ServiceResult<AttachmentsMetadata> result =
            service.addAttachment(new MockMultipartFile("testMultipart.txt",
            "testMultipart.txt", "text/plain", Files.readAllBytes(rsc.getFile().toPath())),
                accessUrl, requestID, reasonId);

        AttachmentsMetadata expectedMetadata = new AttachmentsMetadata(accessUrl, "scanned");
        ServiceResult<AttachmentsMetadata> expectedResult =
            ServiceResult.accepted(expectedMetadata);

        assertEquals(accessUrl, result.getData().getAccessUrl());
        assertNotNull(result.getData().getId());
        assertEquals(result.getData().getScanResult(), "scanned");
        assertEquals(ServiceResultStatus.ACCEPTED, result.getStatus());

        Optional<AttachmentsMetadata> entityMetadata = entity.getReasons()
            .stream()
            .flatMap(reason -> reason.getAttachments().stream())
            .findAny();
        assertTrue(entityMetadata.isPresent());
        assertEquals(expectedMetadata.getAccessUrl(), entityMetadata.get().getAccessUrl());
        assertNotNull(entityMetadata.get().getId());

        verify(repo).save(entity);
        verify(repo).findById("123");
    }
}
