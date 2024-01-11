package uk.gov.companieshouse.extensions.api.attachments;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.extensions.api.Utils.Utils;
import uk.gov.companieshouse.extensions.api.attachments.file.FileTransferApiClientResponse;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.rest.response.PluggableResponseEntityFactory;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("UnitTest")
@ExtendWith(MockitoExtension.class)
public class AttachmentsControllerUnitTest {

    private static final String ATTACHMENT_ID = "123";
    private static final String REQUEST_ID = "ABC";
    private static final String REASON_ID = "MNB";

    @Mock
    private AttachmentsService attachmentsService;

    @Mock
    private HttpServletRequest servletRequest;

    @Mock
    private ApiLogger logger;

    private final ServiceException serviceException = new ServiceException("exception error");

    @Test
    public void willReturn404IfInvalidRequestSuppliedPostRequest() throws Exception {
        when(servletRequest.getRequestURI()).thenReturn("url");
        when(attachmentsService.addAttachment(any(MultipartFile.class), anyString(), anyString(), anyString()))
            .thenThrow(serviceException);

        AttachmentsController controller = new AttachmentsController(
            PluggableResponseEntityFactory.buildWithStandardFactories(), attachmentsService, logger);

        ResponseEntity entity = controller.uploadAttachmentToRequest(Utils.mockMultipartFile(), "123", "1234",
            servletRequest);

        verify(logger).error(serviceException);
        assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
    }

    @Test
    public void willReturn404IfInvalidRequestSuppliedDeleteRequest() throws Exception {
        when(attachmentsService.removeAttachment(anyString(), anyString(), anyString())).thenThrow(serviceException);

        AttachmentsController controller = new AttachmentsController(
            PluggableResponseEntityFactory.buildWithStandardFactories(), attachmentsService, logger);

        ResponseEntity entity = controller.deleteAttachmentFromRequest("123", "1234", "12345");

        verify(logger).info(serviceException.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
    }

    @Test
    public void willReturnStatusFromDownload() {
        HttpServletResponse response = new MockHttpServletResponse();
        FileTransferApiClientResponse dummyDownloadResponse = Utils.dummyDownloadResponse();
        dummyDownloadResponse.setHttpStatus(HttpStatus.NOT_FOUND);

        when(attachmentsService.downloadAttachment(ATTACHMENT_ID, response))
            .thenReturn(dummyDownloadResponse);

        AttachmentsController controller = new AttachmentsController(
            PluggableResponseEntityFactory.buildWithStandardFactories(), attachmentsService, logger);

        ResponseEntity responseEntity = controller.downloadAttachmentFromRequest(ATTACHMENT_ID, response);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody());
        assertTrue(responseEntity.getHeaders().isEmpty());
    }

    @Test
    public void willReturn415FromInvalidUpload() throws ServiceException, IOException {
        HttpClientErrorException expectedException =
            new HttpClientErrorException(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        when(servletRequest.getRequestURI()).thenReturn("url");
        when(attachmentsService.addAttachment(any(MultipartFile.class), anyString(), anyString(),
            anyString())).thenThrow(expectedException);

        AttachmentsController controller =
            new AttachmentsController(PluggableResponseEntityFactory.buildWithStandardFactories(),
                attachmentsService, logger);

        ResponseEntity entity = controller.uploadAttachmentToRequest(Utils.mockMultipartFile(),
            "123", "1234", servletRequest);

        verify(logger).error(anyString(), eq(expectedException));
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, entity.getStatusCode());
    }

    @Test
    public void willReturn500FromFileTransferServerError() throws ServiceException, IOException {
        HttpServerErrorException expectedException =
            new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        when(servletRequest.getRequestURI()).thenReturn("url");
        when(attachmentsService.addAttachment(any(MultipartFile.class), anyString(), anyString(),
            anyString())).thenThrow(expectedException);

        AttachmentsController controller =
            new AttachmentsController(PluggableResponseEntityFactory.buildWithStandardFactories(),
                attachmentsService, logger);

        ResponseEntity entity = controller.uploadAttachmentToRequest(Utils.mockMultipartFile(),
            "123", "1234", servletRequest);

        verify(logger).error(anyString(), eq(expectedException));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, entity.getStatusCode());
    }

    @Test
    public void willCatchHttpClientExceptions_download() throws ServiceException, IOException {
        HttpServletResponse response = new MockHttpServletResponse();

        when(attachmentsService.downloadAttachment(ATTACHMENT_ID, response))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        AttachmentsController controller = new AttachmentsController(
            PluggableResponseEntityFactory.buildWithStandardFactories(), attachmentsService, logger);

        ResponseEntity responseEntity = controller.downloadAttachmentFromRequest(ATTACHMENT_ID, response);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody());
        assertTrue(responseEntity.getHeaders().isEmpty());
    }

    @Test
    public void willCatchHttpServerExceptions_download() throws ServiceException, IOException {
        HttpServletResponse response = new MockHttpServletResponse();

        when(attachmentsService.downloadAttachment(ATTACHMENT_ID, response))
            .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        AttachmentsController controller = new AttachmentsController(
            PluggableResponseEntityFactory.buildWithStandardFactories(), attachmentsService, logger);

        ResponseEntity responseEntity = controller.downloadAttachmentFromRequest(ATTACHMENT_ID, response);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody());
        assertTrue(responseEntity.getHeaders().isEmpty());
    }
}
