package uk.gov.companieshouse.extensions.api.authorization;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import uk.gov.companieshouse.extensions.api.attachments.AttachmentsController;
import uk.gov.companieshouse.extensions.api.attachments.AttachmentsService;
import uk.gov.companieshouse.extensions.api.attachments.file.FileTransferApiClientResponse;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;
import uk.gov.companieshouse.extensions.api.requests.ERICHeaderParser;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestMapper;
import uk.gov.companieshouse.service.rest.response.PluggableResponseEntityFactory;

@Tag("IntegrationTest")
@WebMvcTest(value = {AttachmentsController.class})
@TestPropertySource({"classpath:test.properties"})
public class AuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AttachmentsService attachmentsService;

    @MockitoBean
    private ERICHeaderParser headerParser;

    @MockitoBean
    private ExtensionRequestMapper mapper;

    @MockitoBean
    private ApiLogger logger;

    @MockitoBean
    private PluggableResponseEntityFactory responseEntityFactory;

    @BeforeEach
    public void setup() {
        FileTransferApiClientResponse transferResponse = new FileTransferApiClientResponse();
        transferResponse.fileId("123");
        transferResponse.httpStatus(HttpStatus.OK);
    }

    @Test
    public void willNotAllowUserToDownload() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .get("/company/00006400/extensions/requests/1/reasons/2/attachments/3/download")
            .accept(MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
    }

    @Test
    public void willAllowAdminToDownloadWithCorrectPermissions() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .get("/company/00006400/extensions/requests/1/reasons/2/attachments/3/download")
            .accept(MediaType.APPLICATION_JSON)
            .header("ERIC-Authorised-Scope", "")
            .header("ERIC-Authorised-Roles", "/admin/extensions-download /admin/extensions-view");

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        Assertions.assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    public void willRejectAdminDownloadIfNoDownloadPermission() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .get("/company/00006400/extensions/requests/1/reasons/2/attachments/3/download")
            .accept(MediaType.APPLICATION_JSON)
            .header("ERIC-Authorised-Scope", "")
            .header("ERIC-Authorised-Roles", "/admin/extensions-view");

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
    }

    @Test
    public void willRejectAdminToDelete() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .delete("/company/00006400/extensions/requests/1/reasons/2/attachments/3")
            .accept(MediaType.APPLICATION_JSON)
            .header("ERIC-Authorised-Roles", "/admin/extensions-download /admin/extensions-view");

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
    }
}
