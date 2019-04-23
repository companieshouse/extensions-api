package uk.gov.companieshouse.extensions.api.attachments;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.extensions.api.requests.ERICHeaderParser;
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.rest.response.PluggableResponseEntityFactory;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AttachmentsControllerIntegrationTest {

    private static final String ROOT_URL = "/company/00006400/extensions/requests/a1" +
        "/reasons/a2/attachments";
    private static final String SPECIFIC_URL = "/company/00006400/extensions/requests/a1/reasons" +
        "/a2/attachments/a3";

    private MockMvc mockMvc;

    @Mock
    private AttachmentsService attachmentsService;

    private AttachmentsController controller;

    @Autowired
    private PluggableResponseEntityFactory responseEntityFactory;

    @Autowired
    private ERICHeaderParser parser;

    @Before
    public void setup() {
        controller = new AttachmentsController(responseEntityFactory, attachmentsService, parser);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void testUploadAttachmentToRequest() throws Exception {
         File file = new File("./src/test/resources/input/test.txt");
         MockMultipartFile multipartFile = new MockMultipartFile("file", new FileInputStream(file));

         HashMap<String, String> contentTypeParams = new HashMap<String, String>();
         MediaType mediaType = new MediaType("multipart", "form-data", contentTypeParams);

         when(attachmentsService.addAttachment(any(MultipartFile.class),
                anyString(), anyString(), anyString()))
            .thenReturn(ServiceResult
                .accepted(AttachmentDTO
                    .builder()
                    .withFile(multipartFile)
                    .withAttachment(new Attachment())
                    .build()));

         RequestBuilder requestBuilder = MockMvcRequestBuilders.multipart(ROOT_URL)
                 .file("file", multipartFile.getBytes())
                 .contentType(mediaType)
                 .accept(MediaType.APPLICATION_JSON);

         MvcResult result = mockMvc.perform(requestBuilder).andReturn();
         String expectedJsonResponse = new ObjectMapper()
                 .writer()
                 .writeValueAsString(AttachmentDTO.builder()
                    .withFile(multipartFile)
                    .withAttachment(new Attachment())
                    .build());
         assertEquals(expectedJsonResponse, result.getResponse().getContentAsString());
         assertEquals(HttpStatus.ACCEPTED.value(), result.getResponse().getStatus());
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
