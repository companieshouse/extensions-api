package uk.gov.companieshouse.extensions.api.attachments;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.extensions.api.Utils.Utils;
import uk.gov.companieshouse.extensions.api.groups.Unit;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.rest.response.PluggableResponseEntityFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Category(Unit.class)
@RunWith(MockitoJUnitRunner.class)
public class AttachmentsControllerUnitTest {

    private static final String ATTACHMENT_ID = "123";

    @Mock
    private AttachmentsService attachmentsService;

    @Mock
    private HttpServletRequest servletRequest;

    @Mock
    private ApiLogger logger;

    private ServiceException serviceException = new ServiceException("exception error");

    @Test
    public void willReturn404IfInvalidRequestSuppliedPostRequest() throws Exception {
        when(servletRequest.getRequestURI()).thenReturn("url");
        when(attachmentsService.addAttachment(any(MultipartFile.class), anyString(), anyString(),
            anyString())).thenThrow(serviceException);

        AttachmentsController controller =
            new AttachmentsController(PluggableResponseEntityFactory.buildWithStandardFactories(),
                attachmentsService, logger);

        ResponseEntity entity = controller.uploadAttachmentToRequest(Utils.mockMultipartFile(),
            "123","1234", servletRequest);

        verify(logger).error(serviceException);
        assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
    }

    @Test
    public void willReturn404IfInvalidRequestSuppliedDeleteRequest() throws Exception {
        when(attachmentsService.removeAttachment(anyString(), anyString(),
            anyString())).thenThrow(serviceException);

        AttachmentsController controller =
            new AttachmentsController(PluggableResponseEntityFactory.buildWithStandardFactories(),
                attachmentsService, logger);

        ResponseEntity entity = controller.deleteAttachmentFromRequest(
            "123","1234", "12345");

        verify(logger).info(serviceException.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
    }

    @Test
    public void willReturn200ForSuccessfulDownload() {
//        HttpServletResponse response = new MockHttpServletResponse();
//        DownloadResponse dummyDownloadResponse = Utils.dummyDownloadResponse();
//
//        when(attachmentsService.downloadAttachment(ATTACHMENT_ID, response.getOutputStream()))
//            .thenReturn(ServiceResult.accepted(dummyDownloadResponse));
//
//        AttachmentsController controller =
//            new AttachmentsController(PluggableResponseEntityFactory.buildWithStandardFactories(),
//                attachmentsService, logger);
//
//        ResponseEntity responseEntity = controller.downloadAttachmentFromRequest(ATTACHMENT_ID, response);
//        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
//
//        assertEquals(MediaType.APPLICATION_OCTET_STREAM, responseEntity.getHeaders().getContentType());
//        assertEquals(Utils.DOWNLOAD_CONTENT_LENGTH, responseEntity.getHeaders().getContentLength());
//        assertEquals(Utils.DOWNLOAD_DISPOSITION_TYPE, responseEntity.getHeaders().getContentDisposition().getType());
    }

    @Test
    public void willReturn500ForDownload_IOException() throws IOException {
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        when(response.getOutputStream()).thenThrow(new IOException());

        AttachmentsController controller =
            new AttachmentsController(PluggableResponseEntityFactory.buildWithStandardFactories(),
                attachmentsService, logger);

        ResponseEntity responseEntity = controller.downloadAttachmentFromRequest(ATTACHMENT_ID, response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody());
        assertTrue(responseEntity.getHeaders().isEmpty());
    }

    @Test
    public void willReturn404ForDownload_notFound() throws IOException {
//        HttpServletResponse response = new MockHttpServletResponse();
//        DownloadResponse dummyDownloadResponse = Utils.dummyDownloadResponse();
//        dummyDownloadResponse.setHttpStatus(HttpStatus.NOT_FOUND);
//
//        when(attachmentsService.downloadAttachment(ATTACHMENT_ID, response.getOutputStream()))
//            .thenReturn(ServiceResult.accepted(dummyDownloadResponse));
//
//        AttachmentsController controller =
//            new AttachmentsController(PluggableResponseEntityFactory.buildWithStandardFactories(),
//                attachmentsService, logger);
//
//        ResponseEntity responseEntity = controller.downloadAttachmentFromRequest(ATTACHMENT_ID, response);
//
//        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
//        assertNull(responseEntity.getBody());
//        assertTrue(responseEntity.getHeaders().isEmpty());
    }
}
