package uk.gov.companieshouse.extensions.api.reasons;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.*;

@RunWith(MockitoJUnitRunner.class)
public class ReasonControllerUnitTest {

    @InjectMocks
    private ReasonsController reasonsController;

    @Mock
    private ReasonsService reasonsService;

    @Mock
    private ExtensionReasonMapper extensionReasonMapper;

    @Test
    public void addReasonToRequest() {
        ExtensionCreateReason dummyCreateReason = dummyCreateReason();
        ExtensionReasonEntity dummyReasonEntity = dummyReasonEntity();
        ExtensionReasonDTO dummyReasonDTO = dummyReasonDTO();

        when(reasonsService.insertExtensionsReason(dummyCreateReason)).thenReturn(dummyReasonEntity);
        when(extensionReasonMapper.entityToDTO(dummyReasonEntity)).thenReturn(dummyReasonDTO);
        ResponseEntity<ExtensionReasonDTO> response = reasonsController.addReasonToRequest(dummyCreateReason, "1234");

        assertNotNull(response.getBody());
        assertEquals(dummyReasonDTO.toString(), response.getBody().toString());
    }

    @Test
    public void deleteReasonPlaceholderTest() {
        boolean response = reasonsController.deleteReasonFromRequest("234", "234");
        assertFalse(response);
    }

    @Test
    public void updateReasonPlaceholderTest() {
        String response = reasonsController.updateReasonOnRequest(dummyCreateReason(), "1234", "");
        assertEquals("ExtensionReason updated: Extension create reason illness Additional text: string  " +
            "Date start: 2018-12-12  Date end: 2019-12-12", response);
    }

}
