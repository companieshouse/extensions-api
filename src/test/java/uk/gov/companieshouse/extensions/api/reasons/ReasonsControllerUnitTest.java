package uk.gov.companieshouse.extensions.api.reasons;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestFullEntity;
import uk.gov.companieshouse.extensions.api.requests.ExtensionsLinkKeys;
import uk.gov.companieshouse.extensions.api.response.ListResponse;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.links.Links;
import uk.gov.companieshouse.service.rest.response.ChResponseBody;
import uk.gov.companieshouse.service.rest.response.PluggableResponseEntityFactory;

import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.*;

@Tag("UnitTest")
@ExtendWith(MockitoExtension.class)
public class ReasonsControllerUnitTest {

    @InjectMocks
    private ReasonsController reasonsController;

    @Mock
    private ReasonsService reasonsService;

    @Mock
    private HttpServletRequest mockHttpServletRequest;

    @Mock
    private ApiLogger logger;


    @Test
    public void returns404IfServiceThrows() throws ServiceException {
        PluggableResponseEntityFactory testFactory =
            PluggableResponseEntityFactory.buildWithStandardFactories();
        ResponseEntity<ChResponseBody<List<ExtensionReasonDTO>>> expectedEntity =
            testFactory.createResponse(ServiceResult.notFound());

        ReasonsController controller = new ReasonsController(reasonsService, logger);
        ServiceException serviceException = new ServiceException("");
        when(reasonsService.getReasons(REQUEST_ID))
            .thenThrow(serviceException);
        ResponseEntity<ListResponse<ExtensionReasonDTO>> response =
            controller.getReasons(REQUEST_ID);

        verify(reasonsService).getReasons(REQUEST_ID);
        verify(logger).info(serviceException.getMessage());
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assertions.assertEquals(expectedEntity, response);
    }

    @Test
    public void addReasonToRequest() throws ServiceException {
        ExtensionCreateReason dummyCreateReason = dummyCreateReason();

        ExtensionRequestFullEntity dummyRequestEntity = dummyRequestEntity();
        dummyRequestEntity.addReason(dummyReasonEntity());

        ExtensionReasonDTO dto = new ExtensionReasonDTO();
        dto.setId("123");

        Links links = new Links();
        links.setLink(ExtensionsLinkKeys.SELF, "requestURL");
        dto.setLinks(links);

        when(reasonsService.addExtensionsReasonToRequest(dummyCreateReason, REQUEST_ID,
            mockHttpServletRequest.getRequestURI())).thenReturn(ServiceResult.created(dto));
        ResponseEntity<ExtensionReasonDTO> response =
            reasonsController.addReasonToRequest(dummyCreateReason, REQUEST_ID, mockHttpServletRequest);

        Assertions.assertNotNull(response.getBody());
        Assertions.assertEquals(dto.getLinks(), response.getBody().getLinks());
        Assertions.assertEquals(dto.getId(), response.getBody().getId());
        Assertions.assertEquals("requestURL", Objects.requireNonNull(response.getHeaders().getLocation()).toString());
    }

    @Test
    public void deleteReasonFromRequest() {
        ExtensionRequestFullEntity dummyRequestEntity = dummyRequestEntity();

        when(reasonsService.removeExtensionsReasonFromRequest(REQUEST_ID, REASON_ID)).thenReturn(dummyRequestEntity);

        ResponseEntity<ExtensionReasonDTO> response = reasonsController.deleteReasonFromRequest
            (REQUEST_ID, REASON_ID);

        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void canPatchAReason() throws ServiceException {
        ExtensionReasonDTO dto = new ExtensionReasonDTO();

        when(reasonsService.patchReason(any(ExtensionCreateReason.class), anyString(), anyString()))
            .thenReturn(dto);

        ResponseEntity<ExtensionReasonDTO> response =
            reasonsController.patchReason(dummyCreateReason(), "1234", "");
        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(dto.toString(), response.getBody().toString());
    }

}
