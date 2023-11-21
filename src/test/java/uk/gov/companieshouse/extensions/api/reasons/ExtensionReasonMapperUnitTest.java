package uk.gov.companieshouse.extensions.api.reasons;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.extensions.api.attachments.Attachment;
import uk.gov.companieshouse.extensions.api.requests.ExtensionsLinkKeys;

import java.util.Map;

import static uk.gov.companieshouse.extensions.api.Utils.Utils.dummyAttachment;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.dummyReasonEntity;

@Tag("UnitTest")
public class ExtensionReasonMapperUnitTest {

    @Test
    public void canMapEntityToDTO() {
        ExtensionReasonMapper extensionReasonMapper = new ExtensionReasonMapper();
        ExtensionReasonEntity extensionReasonEntity = dummyReasonEntity();
        Attachment attachment = dummyAttachment();
        extensionReasonEntity.addAttachment(attachment);

        ExtensionReasonDTO extensionReasonDTO = extensionReasonMapper.entityToDTO(extensionReasonEntity);

        Assertions.assertNotNull(extensionReasonDTO);
        Assertions.assertEquals(extensionReasonEntity.getReasonInformation(), extensionReasonDTO.getReasonInformation());
        Assertions.assertEquals(extensionReasonEntity.getStartOn(), extensionReasonDTO.getStartOn());
        Assertions.assertEquals(extensionReasonEntity.getEndOn(), extensionReasonDTO.getEndOn());
        Assertions.assertEquals(extensionReasonEntity.getReason(), extensionReasonDTO.getReason());
        Assertions.assertEquals(extensionReasonEntity.getReasonStatus(), extensionReasonDTO.getReasonStatus());

        String attachmentName = attachment.getName();
        Map<String, String> dtoAttachmentLinks = extensionReasonDTO.getAttachments().getLinks();
        Assertions.assertTrue(dtoAttachmentLinks.containsKey(attachmentName));
        Assertions.assertEquals(attachment.getLinks().getLink(ExtensionsLinkKeys.SELF), dtoAttachmentLinks.get(attachmentName));
    }
}
