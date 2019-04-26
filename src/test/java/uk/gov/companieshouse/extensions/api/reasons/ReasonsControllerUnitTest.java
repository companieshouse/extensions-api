package uk.gov.companieshouse.extensions.api.reasons;

import javafx.beans.binding.When;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestFullDTO;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestFullEntity;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestMapper;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestsRepository;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.*;

@RunWith(MockitoJUnitRunner.class)
public class ReasonsControllerUnitTest {

    @InjectMocks
    private ReasonsController reasonsController;

    @Mock
    private ReasonsService reasonsService;

    @Mock
    private ExtensionRequestMapper extensionRequestMapper;

    @Mock
    private HttpServletRequest mockHttpServletRequest;

    @Before
    public void setup() {
        when(mockHttpServletRequest.getRequestURI()).thenReturn(BASE_URL);
    }

    @Test
    public void addReasonToRequest() {
        ExtensionCreateReason dummyCreateReason = dummyCreateReason();

        ExtensionRequestFullEntity dummyRequestEntity = dummyRequestEntity();
        dummyRequestEntity.addReason(dummyReasonEntity());

        ExtensionRequestFullDTO entityRequestDTO = dummyRequestDTO();

        when(reasonsService.addExtensionsReasonToRequest(dummyCreateReason, REQUEST_ID, mockHttpServletRequest.getRequestURI())).thenReturn(dummyRequestEntity);
        when(extensionRequestMapper.entityToDTO(dummyRequestEntity)).thenReturn(entityRequestDTO);
        ResponseEntity<ExtensionRequestFullDTO> response = reasonsController.addReasonToRequest(dummyCreateReason, REQUEST_ID, mockHttpServletRequest);

        assertNotNull(response.getBody());
        assertEquals(entityRequestDTO.toString(), response.getBody().toString());
    }

    @Test
    public void deleteReasonFromRequest() {
        ExtensionRequestFullEntity dummyRequestEntity = dummyRequestEntity();
        ExtensionRequestFullDTO entityRequestDTO = dummyRequestDTO();

        when(reasonsService.removeExtensionsReasonFromRequest(REQUEST_ID, REASON_ID)).thenReturn(dummyRequestEntity);
        when(extensionRequestMapper.entityToDTO(dummyRequestEntity)).thenReturn(entityRequestDTO);

        ResponseEntity<ExtensionRequestFullDTO> response = reasonsController.deleteReasonFromRequest
            (REQUEST_ID, REASON_ID);

        assertNotNull(response.getBody());
        assertEquals(entityRequestDTO.toString(), response.getBody().toString());
    }

    @Test
    public void updateReasonPlaceholderTest() {
        String response = reasonsController.updateReasonOnRequest(dummyCreateReason(), "1234", "");
        assertEquals("ExtensionReason updated: Extension create reason illness Additional text: string  " +
            "Date start: 2018-12-12  Date end: 2019-12-12", response);
    }

}
