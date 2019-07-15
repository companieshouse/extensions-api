package uk.gov.companieshouse.extensions.api.reasons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.BASE_URL;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.REASON_ID;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.REQUEST_ID;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.dummyCreateReason;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.dummyReasonEntity;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.dummyRequestEntity;

import java.util.List;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import uk.gov.companieshouse.extensions.api.groups.Unit;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestFullEntity;
import uk.gov.companieshouse.extensions.api.requests.ExtensionsLinkKeys;
import uk.gov.companieshouse.extensions.api.response.ListResponse;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.links.Links;
import uk.gov.companieshouse.service.rest.response.ChResponseBody;
import uk.gov.companieshouse.service.rest.response.PluggableResponseEntityFactory;

@Category(Unit.class)
@RunWith(MockitoJUnitRunner.class)
public class ReasonsControllerUnitTest {

    @InjectMocks
    private ReasonsController reasonsController;

    @Mock
    private ReasonsService reasonsService;

    @Mock
    private HttpServletRequest mockHttpServletRequest;

    @Mock
    private ApiLogger logger;

    @Before
    public void setup() {
        when(mockHttpServletRequest.getRequestURI()).thenReturn(BASE_URL);
    }

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
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(expectedEntity, response);
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

        assertNotNull(response.getBody());
        assertEquals(dto.getLinks(), response.getBody().getLinks());
        assertEquals(dto.getId(), response.getBody().getId());
        assertEquals("requestURL", Objects.requireNonNull(response.getHeaders().getLocation()).toString());
    }

    @Test
    public void deleteReasonFromRequest() {
        ExtensionRequestFullEntity dummyRequestEntity = dummyRequestEntity();

        when(reasonsService.removeExtensionsReasonFromRequest(REQUEST_ID, REASON_ID)).thenReturn(dummyRequestEntity);

        ResponseEntity<ExtensionReasonDTO> response = reasonsController.deleteReasonFromRequest
            (REQUEST_ID, REASON_ID);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void canPatchAReason() throws ServiceException {
        ExtensionReasonDTO dto = new ExtensionReasonDTO();

        when(reasonsService.patchReason(any(ExtensionCreateReason.class), anyString(), anyString()))
            .thenReturn(dto);

        ResponseEntity<ExtensionReasonDTO> response =
            reasonsController.patchReason(dummyCreateReason(), "1234", "");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto.toString(), response.getBody().toString());
    }

}
