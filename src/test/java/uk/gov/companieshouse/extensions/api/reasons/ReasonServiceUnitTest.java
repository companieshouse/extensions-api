package uk.gov.companieshouse.extensions.api.reasons;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;

import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestFullEntity;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestsRepository;
import uk.gov.companieshouse.extensions.api.requests.RequestsService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.*;


@RunWith(MockitoJUnitRunner.class)
public class ReasonServiceUnitTest {

    @InjectMocks
    private ReasonsService reasonService;

    @Mock
    private RequestsService requestsService;

    @Mock
    private ExtensionRequestsRepository extensionRequestsRepository;

    @Captor
    private ArgumentCaptor<ExtensionRequestFullEntity> captor;

    @Test
    public void testCorrectDataIsPassedToAddExtensionsReasonToRequest() {
        ExtensionRequestFullEntity extensionRequestFullEntity = dummyRequestEntity();
        when(requestsService.getExtensionsRequestById(REQUEST_ID)).thenReturn(extensionRequestFullEntity);
        when(extensionRequestsRepository.save(extensionRequestFullEntity)).thenReturn(extensionRequestFullEntity);

        ExtensionCreateReason dummyCreateReason = dummyCreateReason();
        reasonService.addExtensionsReasonToRequest(dummyCreateReason, REQUEST_ID, "");
        verify(extensionRequestsRepository, times(1)).save(captor.capture());
        ExtensionRequestFullEntity extensionRequestResult = captor.getValue();
        ExtensionReasonEntity extensionReasonResult = extensionRequestResult.getReasons().get(0);

        assertNotNull(extensionReasonResult);
        assertEquals(dummyCreateReason.getAdditionalText(), extensionReasonResult.getAdditionalText());
        assertEquals(dummyCreateReason.getStartOn(), extensionReasonResult.getStartOn());
        assertEquals(dummyCreateReason.getEndOn(), extensionReasonResult.getEndOn());
        assertEquals(dummyCreateReason.getReason(), extensionReasonResult.getReason());
    }
}
