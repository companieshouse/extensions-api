package uk.gov.companieshouse.extensions.api.attachments;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import uk.gov.companieshouse.extensions.api.Utils.Utils;
import uk.gov.companieshouse.extensions.api.attachments.file.FileTransferApiClientResponse;
import uk.gov.companieshouse.extensions.api.groups.Integration;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.rest.response.PluggableResponseEntityFactory;

@Tag("Integration")
@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AttachmentsControllerIntegrationTest {

    private static final String ROOT_URL = "/company/00006400/extensions/requests/a1" +
        "/reasons/a2/attachments";
    private static final String SPECIFIC_URL = "/company/00006400/extensions/requests/a1/reasons" +
        "/a2/attachments/a3";
    private static final String DOWNLOAD_URL = SPECIFIC_URL + "/download";

    private MockMvc mockMvc;

    @Mock
    private AttachmentsService attachmentsService;

    @Mock
    private ApiLogger logger;

    @Autowired
    private PluggableResponseEntityFactory responseEntityFactory;

    @BeforeEach
    public void setup() {
        AttachmentsController controller = new AttachmentsController(responseEntityFactory, attachmentsService, logger);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    public void testUploadAttachmentToRequest() throws Exception {
         File file = new File("./src/test/resources/input/test.txt");
         MockMultipartFile multipartFile = new MockMultipartFile("file", new FileInputStream(file));

         HashMap<String, String> contentTypeParams = new HashMap<>();
         MediaType mediaType = new MediaType("multipart", "form-data", contentTypeParams);

         AttachmentDTO expectedDto = AttachmentDTO
             .builder()
             .withFile(multipartFile)
             .withAttachment(new Attachment())
             .build();
         when(attachmentsService.addAttachment(any(MultipartFile.class),
                anyString(), anyString(), anyString()))
            .thenReturn(ServiceResult.accepted(expectedDto));

         RequestBuilder requestBuilder = MockMvcRequestBuilders.multipart(ROOT_URL)
                 .file("file", multipartFile.getBytes())
                 .contentType(mediaType)
                 .accept(MediaType.APPLICATION_JSON);

         MvcResult result = mockMvc.perform(requestBuilder).andReturn();
         String expectedJsonResponse = new ObjectMapper()
                 .writer()
                 .writeValueAsString(expectedDto);
         assertEquals(expectedJsonResponse, result.getResponse().getContentAsString());
         assertEquals(HttpStatus.ACCEPTED.value(), result.getResponse().getStatus());
    }

    @Test
    public void testDeleteAttachmentFromRequest() throws Exception {
        when(attachmentsService.removeAttachment(anyString(), anyString(), anyString()))
            .thenReturn(ServiceResult.deleted());
        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .delete(SPECIFIC_URL)
            .accept(MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        assertEquals(HttpStatus.NO_CONTENT.value(), result.getResponse().getStatus());
    }

    @Test
    public void testDownloadAttachmentFromRequest() throws Exception {
        FileTransferApiClientResponse dummyDownloadResponse = Utils.dummyDownloadResponse();

        when(attachmentsService.downloadAttachment(anyString(), any(HttpServletResponse.class)))
            .thenReturn(dummyDownloadResponse);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(DOWNLOAD_URL);

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    public void testDownloadAttachmentFromRequest_error() throws Exception {
        FileTransferApiClientResponse dummyDownloadResponse = new FileTransferApiClientResponse();
        dummyDownloadResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);

        when(attachmentsService.downloadAttachment(anyString(), any(HttpServletResponse.class)))
            .thenReturn(dummyDownloadResponse);

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(DOWNLOAD_URL);

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getResponse().getStatus());
    }
}
