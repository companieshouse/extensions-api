package uk.gov.companieshouse.extensions.api.attachments;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.rest.response.ChResponseBody;
import uk.gov.companieshouse.service.rest.response.PluggableResponseEntityFactory;

@Tag("IntegrationTest")
@ExtendWith(SpringExtension.class)
class AttachmentsControllerIntegrationTest {

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

    @Mock
    private PluggableResponseEntityFactory responseEntityFactory;


    @BeforeEach
    void setup() {
        AttachmentsController controller = new AttachmentsController(responseEntityFactory, attachmentsService, logger);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testUploadAttachmentToRequest() throws Exception {
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
        ServiceResult<AttachmentDTO> resultDTO = ServiceResult.accepted(expectedDto);
        when(responseEntityFactory.createResponse(any())).thenReturn(createResponseEntityForFile(resultDTO));

        RequestBuilder requestBuilder = MockMvcRequestBuilders.multipart(ROOT_URL)
            .file("file", multipartFile.getBytes())
            .contentType(mediaType)
            .accept(MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("\"etag\":\"\""));
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("\"name\":\"\""));
        Assertions.assertTrue(result.getResponse().getContentAsString().contains("\"size\":123"));
        Assertions.assertEquals(HttpStatus.ACCEPTED.value(), result.getResponse().getStatus());
    }

    @Test
    void testDeleteAttachmentFromRequest() throws Exception {
        when(attachmentsService.removeAttachment(anyString(), anyString(), anyString()))
            .thenReturn(ServiceResult.deleted());
        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .delete(SPECIFIC_URL)
            .accept(MediaType.APPLICATION_JSON);
        when(responseEntityFactory.createResponse(any())).thenReturn(ResponseEntity.status(HttpStatus.NO_CONTENT).build());
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        Assertions.assertEquals(HttpStatus.NO_CONTENT.value(), result.getResponse().getStatus());
    }

    @Test
    void testDownloadAttachmentFromRequest() throws Exception {

        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(DOWNLOAD_URL);

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        Assertions.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    <T> ResponseEntity<ChResponseBody<Object>> createResponseEntityForFile(
        ServiceResult<T> serviceResult) {
        ChResponseBody<T> body = ChResponseBody.createNormalBody(serviceResult.getData());
        return ResponseEntity.accepted().body((ChResponseBody<Object>) body);
    }
}
