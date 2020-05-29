package uk.gov.companieshouse.extensions.api.attachments;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import uk.gov.companieshouse.extensions.api.Utils.Utils;
import uk.gov.companieshouse.extensions.api.requests.ExtensionsLinkKeys;
import uk.gov.companieshouse.service.links.Links;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("Unit")
public class AttachmentDTOUnitTest {

    @Test
    public void jacksonSerializationIgnoresBuilder() throws IOException {
        AttachmentDTO attachmentDTO = AttachmentDTO.builder()
            .withAttachment(new Attachment())
            .withFile(Utils.mockMultipartFile())
            .build();

        ObjectMapper mapper = new ObjectMapper();
        String actualJson = mapper.writeValueAsString(attachmentDTO);
        String expectedJson = "{\"size\":10,\"name\":\"" + Utils.ORIGINAL_FILE_NAME+"\"," +
            "\"contentType\":\"text/plain\",\"etag\":\"\"}";
        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void canBuildAttachmentDTO() throws IOException {
        Attachment attachment = new Attachment();
        attachment.setId("12345");
        Links links = new Links();
        links.setLink(ExtensionsLinkKeys.SELF, "linkToSelf");
        MultipartFile file = Utils.mockMultipartFile();

        AttachmentDTO attachmentDTO = AttachmentDTO
            .builder()
            .withFile(file)
            .withAttachment(attachment)
            .withLinks(links)
            .build();

        assertEquals("12345", attachmentDTO.getId());
        assertEquals(Utils.ORIGINAL_FILE_NAME, attachmentDTO.getName());
        assertEquals("linkToSelf", attachmentDTO.getLinks().getLink(ExtensionsLinkKeys.SELF));
    }
}
