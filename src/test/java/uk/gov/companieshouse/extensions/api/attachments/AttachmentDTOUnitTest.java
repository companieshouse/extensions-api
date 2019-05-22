package uk.gov.companieshouse.extensions.api.attachments;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.springframework.web.multipart.MultipartFile;

import uk.gov.companieshouse.extensions.api.Utils.Utils;
import uk.gov.companieshouse.extensions.api.groups.Unit;
import uk.gov.companieshouse.service.links.Links;

@Category(Unit.class)
public class AttachmentDTOUnitTest {

    @Test
    public void jacksonSerializationIgnoresBuilder() throws IOException {
        AttachmentDTO attachmentDTO = AttachmentDTO.builder()
            .withAttachment(new Attachment())
            .withFile(Utils.mockMultipartFile())
            .build();

        ObjectMapper mapper = new ObjectMapper();
        String actualJson = mapper.writeValueAsString(attachmentDTO);
        String expectedJson = "{\"size\":10,\"name\":\"testMultipart.txt\"," +
            "\"contentType\":\"text/plain\",\"etag\":\"\"}";
        assertEquals(expectedJson, actualJson);
    }

    @Test
    public void canBuildAttachmentDTO() throws IOException {
        Attachment attachment = new Attachment();
        attachment.setId("12345");
        Links links = new Links();
        links.setLink(() -> "self", "linkToSelf");
        MultipartFile file = Utils.mockMultipartFile();

        AttachmentDTO attachmentDTO = AttachmentDTO
            .builder()
            .withFile(file)
            .withAttachment(attachment)
            .withLinks(links)
            .build();

        assertEquals("12345", attachmentDTO.getId());
        assertEquals("testMultipart.txt", attachmentDTO.getName());
        assertEquals("linkToSelf", attachmentDTO.getLinks().getLink(() -> "self"));
    }
}
