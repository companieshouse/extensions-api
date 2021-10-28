package uk.gov.companieshouse.extensions.api.reasons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.dummyAttachment;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.dummyReasonEntity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.extensions.api.attachments.Attachment;
import uk.gov.companieshouse.extensions.api.requests.ExtensionsLinkKeys;

import java.util.Map;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ExtensionReasonMapperUnitTest {

    @Test
    public void canMapEntityToDTO() {
        ExtensionReasonMapper extensionReasonMapper = new ExtensionReasonMapper();
        ExtensionReasonEntity extensionReasonEntity = dummyReasonEntity();
        Attachment attachment = dummyAttachment();
        extensionReasonEntity.addAttachment(attachment);

        ExtensionReasonDTO extensionReasonDTO = extensionReasonMapper.entityToDTO(extensionReasonEntity);

        assertNotNull(extensionReasonDTO);
        assertEquals(extensionReasonEntity.getReasonInformation(), extensionReasonDTO.getReasonInformation());
        assertEquals(extensionReasonEntity.getStartOn(), extensionReasonDTO.getStartOn());
        assertEquals(extensionReasonEntity.getEndOn(), extensionReasonDTO.getEndOn());
        assertEquals(extensionReasonEntity.getReason(), extensionReasonDTO.getReason());
        assertEquals(extensionReasonEntity.getReasonStatus(), extensionReasonDTO.getReasonStatus());

        String attachmentName = attachment.getName();
        Map<String, String> dtoAttachmentLinks = extensionReasonDTO.getAttachments().getLinks();
        assertTrue(dtoAttachmentLinks.containsKey(attachmentName));
        assertEquals(attachment.getLinks().getLink(ExtensionsLinkKeys.SELF), dtoAttachmentLinks.get(attachmentName));
    }
}
