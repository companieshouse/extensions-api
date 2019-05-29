package uk.gov.companieshouse.extensions.api.attachments;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import uk.gov.companieshouse.extensions.api.Utils.Utils;
import uk.gov.companieshouse.extensions.api.groups.Unit;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.rest.response.PluggableResponseEntityFactory;

@Category(Unit.class)
@RunWith(MockitoJUnitRunner.class)
public class AttachmentsControllerUnitTest {

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

        verify(logger).info(serviceException.getMessage());
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
        AttachmentsController controller =
            new AttachmentsController(PluggableResponseEntityFactory.buildWithStandardFactories(),
                attachmentsService, logger);
        String attachmentId = "123";
        HttpServletResponse response = new MockHttpServletResponse();
        ResponseEntity responseEntity = controller.downloadAttachmentFromRequest(attachmentId, response);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }
}
