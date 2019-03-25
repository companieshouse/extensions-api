package uk.gov.companieshouse.extensions.api.attachments;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc 
public class AttachmentsControllerTests {

    private static final String ROOT_URL = "/api/extensions/requests/a1/attachments";
    private static final String SPECIFIC_URL = "/api/extensions/requests/a1/attachments/a2";

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testUploadAttachmentToRequest() throws Exception {
         File file = new File("./src/test/resources/input/test.txt");
         MockMultipartFile multipartFile = new MockMultipartFile("file", new FileInputStream(file));

         HashMap<String, String> contentTypeParams = new HashMap<String, String>();
         MediaType mediaType = new MediaType("multipart", "form-data", contentTypeParams);

         RequestBuilder requestBuilder = MockMvcRequestBuilders.multipart(ROOT_URL)
                 .file("file", multipartFile.getBytes())
                 .contentType(mediaType)
                 .accept(MediaType.APPLICATION_JSON);

         MvcResult result = mockMvc.perform(requestBuilder).andReturn();
         String expectedJsonResponse = new ObjectMapper()
                 .writer()
                 .writeValueAsString(new AttachmentsMetadata("/dummy.url", "scanned"));
         assertEquals(expectedJsonResponse, result.getResponse().getContentAsString());
    }

    @Test
    public void testDeleteAttachmentFromRequest() throws Exception {
       RequestBuilder requestBuilder = MockMvcRequestBuilders.delete(
                SPECIFIC_URL).accept(
                    MediaType.APPLICATION_JSON);

         MvcResult result = mockMvc.perform(requestBuilder).andReturn();
         assertEquals("false", result.getResponse().getContentAsString());
    }

    @Test
    public void testDownloadAttachmentFromRequest() throws Exception {
       RequestBuilder requestBuilder = MockMvcRequestBuilders.get(
                SPECIFIC_URL).accept(
                     MediaType.APPLICATION_JSON);

         MvcResult result = mockMvc.perform(requestBuilder).andReturn();
         assertEquals("Getting attachment", result.getResponse().getContentAsString());
    }
}
