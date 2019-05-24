package uk.gov.companieshouse.extensions.api.reasons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.dummyAttachment;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.dummyReasonEntity;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import uk.gov.companieshouse.extensions.api.attachments.Attachment;
import uk.gov.companieshouse.extensions.api.groups.Unit;
import uk.gov.companieshouse.extensions.api.requests.ExtensionsLinkKeys;

import java.util.Map;

@Category(Unit.class)
public class ExtensionReasonMapperUnitTest {

    @Test
    public void canMapEntityToDTO() {
        ExtensionReasonMapper extensionReasonMapper = new ExtensionReasonMapper();
        ExtensionReasonEntity extensionReasonEntity = dummyReasonEntity();
        Attachment attachment = dummyAttachment();
        extensionReasonEntity.addAttachment(attachment);

        ExtensionReasonDTO extensionReasonDTO = extensionReasonMapper.entityToDTO(extensionReasonEntity);

        assertNotNull(extensionReasonDTO);
        assertEquals(extensionReasonEntity.getAdditionalText(), extensionReasonDTO.getAdditionalText());
        assertEquals(extensionReasonEntity.getStartOn(), extensionReasonDTO.getStartOn());
        assertEquals(extensionReasonEntity.getEndOn(), extensionReasonDTO.getEndOn());
        assertEquals(extensionReasonEntity.getReason(), extensionReasonDTO.getReason());

        String attachmentName = attachment.getName();
        Map<String, String> dtoAttachmentLinks = extensionReasonDTO.getAttachments().getLinks();
        assertTrue(dtoAttachmentLinks.containsKey(attachmentName));
        assertEquals(attachment.getLinks().getLink(ExtensionsLinkKeys.SELF), dtoAttachmentLinks.get(attachmentName));
    }
}
