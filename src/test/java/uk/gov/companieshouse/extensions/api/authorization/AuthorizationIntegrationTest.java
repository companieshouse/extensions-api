//package uk.gov.companieshouse.extensions.api.authorization;
//
//import static org.junit.Assert.assertEquals;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.never;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//
//import java.util.Optional;
//
//import jakarta.servlet.http.HttpServletResponse;
//
//import org.junit.Before;
//import org.junit.jupiter.api.Test;
//import org.junit.experimental.categories.Category;
//import org.junit.runner.RunWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//import org.springframework.test.web.servlet.RequestBuilder;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//
//import uk.gov.companieshouse.extensions.api.attachments.AttachmentsController;
//import uk.gov.companieshouse.extensions.api.attachments.AttachmentsService;
//import uk.gov.companieshouse.extensions.api.attachments.file.FileTransferApiClientResponse;
//import uk.gov.companieshouse.extensions.api.groups.Integration;
//import uk.gov.companieshouse.extensions.api.logger.ApiLogger;
//import uk.gov.companieshouse.extensions.api.requests.CreatedBy;
//import uk.gov.companieshouse.extensions.api.requests.ERICHeaderParser;
//import uk.gov.companieshouse.extensions.api.requests.ExtensionCreateRequest;
//import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestFullEntity;
//import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestMapper;
//import uk.gov.companieshouse.extensions.api.requests.ExtensionsLinkKeys;
//import uk.gov.companieshouse.extensions.api.requests.RequestsController;
//import uk.gov.companieshouse.extensions.api.requests.RequestsService;
//import uk.gov.companieshouse.service.links.Links;
//import uk.gov.companieshouse.service.rest.response.PluggableResponseEntityFactory;
//
//@Tag("IntegrationTest")//@RunWith(SpringRunner.class)
//@WebMvcTest(value = { AttachmentsController.class })
//@TestPropertySource({ "classpath:application.properties"})
//public class AuthorizationIntegrationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private AttachmentsService attachmentsService;
//
//    @MockBean
//    private ERICHeaderParser headerParser;
//
//    @MockBean
//    private ExtensionRequestMapper mapper;
//
//    @MockBean
//    private ApiLogger logger;
//
//    @MockBean
//    private PluggableResponseEntityFactory responseEntityFactory;
//
//    @Before
//    public void setup() {
//        FileTransferApiClientResponse transferResponse = new FileTransferApiClientResponse();
//        transferResponse.setFileId("123");
//        transferResponse.setHttpStatus(HttpStatus.OK);
//        when(attachmentsService.downloadAttachment(anyString(), any(HttpServletResponse.class)))
//            .thenReturn(transferResponse);
//    }
//
//    @Test
//    public void willNotAllowUserToDownload() throws Exception {
//        RequestBuilder requestBuilder = MockMvcRequestBuilders
//            .get("/company/00006400/extensions/requests/1/reasons/2/attachments/3/download")
//            .accept(MediaType.APPLICATION_JSON);
//
//        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
//        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
//    }
//
//    @Test
//    public void willAllowAdminToDownloadWithCorrectPermissions() throws Exception {
//        RequestBuilder requestBuilder = MockMvcRequestBuilders
//            .get("/company/00006400/extensions/requests/1/reasons/2/attachments/3/download")
//            .accept(MediaType.APPLICATION_JSON)
//            .header("ERIC-Authorised-Scope", "")
//            .header("ERIC-Authorised-Roles", "/admin/extensions-download /admin/extensions-view");
//
//        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
//        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
//    }
//
//    @Test
//    public void willRejectAdminDownloadIfNoDownloadPermission() throws Exception {
//        RequestBuilder requestBuilder = MockMvcRequestBuilders
//            .get("/company/00006400/extensions/requests/1/reasons/2/attachments/3/download")
//            .accept(MediaType.APPLICATION_JSON)
//            .header("ERIC-Authorised-Scope", "")
//            .header("ERIC-Authorised-Roles", "/admin/extensions-view");
//
//        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
//        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
//    }
//
//    @Test
//    public void willRejectAdminToDelete() throws Exception {
//        RequestBuilder requestBuilder = MockMvcRequestBuilders
//            .delete("/company/00006400/extensions/requests/1/reasons/2/attachments/3")
//            .accept(MediaType.APPLICATION_JSON)
//            .header("ERIC-Authorised-Roles", "/admin/extensions-download /admin/extensions-view");
//
//        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
//        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
//    }
//}
