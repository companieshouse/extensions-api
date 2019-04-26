package uk.gov.companieshouse.extensions.api.attachments;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.extensions.api.Utils.Utils;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.rest.response.PluggableResponseEntityFactory;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AttachmentsControllerUnitTest {

    @Mock
    private AttachmentsService attachmentsService;

    @Mock
    private HttpServletRequest servletRequest;

    @Test
    public void willReturn404IfInvalidRequestSupplied() throws Exception {
        when(servletRequest.getRequestURI()).thenReturn("url");
        when(attachmentsService.addAttachment(any(MultipartFile.class), anyString(), anyString(),
            anyString())).thenThrow(new ServiceException("exception error"));

        AttachmentsController controller =
            new AttachmentsController(PluggableResponseEntityFactory.buildWithStandardFactories(),
                attachmentsService);

        ResponseEntity entity = controller.uploadAttachmentToRequest(Utils.mockMultipartFile(),
            "123","1234", servletRequest);

        assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
    }
}