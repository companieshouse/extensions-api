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
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.ServiceResultStatus;

import java.security.cert.Extension;
import java.time.LocalDate;
import java.util.function.Supplier;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.*;


@RunWith(MockitoJUnitRunner.class)
public class ReasonServiceUnitTest {

    @InjectMocks
    private ReasonsService reasonsService;

    @Mock
    private RequestsService requestsService;

    @Mock
    private ExtensionReasonMapper reasonMapper;

    @Mock
    private ExtensionRequestsRepository extensionRequestsRepository;

    @Mock
    private Supplier<String> mockRandomUUid;

    @Captor
    private ArgumentCaptor<ExtensionRequestFullEntity> captor;

    @Test
    public void testCorrectDataIsPassedToAddExtensionsReasonToRequest() throws ServiceException {

        ExtensionRequestFullEntity extensionRequestFullEntity = dummyRequestEntity();
        when(requestsService.getExtensionsRequestById(REQUEST_ID)).thenReturn(extensionRequestFullEntity);
        when(extensionRequestsRepository.save(any(ExtensionRequestFullEntity.class)))
            .thenReturn(extensionRequestFullEntity);
        when(mockRandomUUid.get())
            .thenReturn("abc");
        ExtensionReasonDTO dto = new ExtensionReasonDTO();
        dto.setId("abc");
        when(reasonMapper.entityToDTO(any(ExtensionReasonEntity.class)))
            .thenReturn(dto);

        ExtensionCreateReason dummyCreateReason = dummyCreateReason();
        ServiceResult<ExtensionReasonDTO> result =
            reasonsService.addExtensionsReasonToRequest(dummyCreateReason,
            REQUEST_ID, "");
        verify(extensionRequestsRepository).save(captor.capture());
        verify(mockRandomUUid).get();
        ExtensionRequestFullEntity extensionRequestResult = captor.getValue();
        ExtensionReasonEntity extensionReasonResult = extensionRequestResult.getReasons().get(0);

        assertNotNull(extensionReasonResult);
        assertEquals("string", extensionReasonResult.getAdditionalText());
        assertEquals("abc", extensionReasonResult.getId());
        assertEquals(dummyCreateReason.getAdditionalText(), extensionReasonResult.getAdditionalText());
        assertEquals(dummyCreateReason.getStartOn(), extensionReasonResult.getStartOn());
        assertEquals(dummyCreateReason.getEndOn(), extensionReasonResult.getEndOn());
        assertEquals(dummyCreateReason.getReason(), extensionReasonResult.getReason());

        assertEquals(ServiceResultStatus.CREATED, result.getStatus());
        assertNotNull(result.getData());
        assertEquals("abc", result.getData().getId());
    }

    @Test
    public void exceptionThrownIfNoRequestFound() {
        when(requestsService.getExtensionsRequestById("123"))
            .thenReturn(null);
        try {
            reasonsService.addExtensionsReasonToRequest(new ExtensionCreateReason(), "123", "url");
            fail();
        } catch (ServiceException e) {
            assertEquals("Request 123 not found", e.getMessage());
        }
    }

    @Test
    public void testReasonIsRemovedFromRequest() {
        ExtensionRequestFullEntity extensionRequestFullEntity = dummyRequestEntity();
        extensionRequestFullEntity.addReason(dummyReasonEntity());

        when(requestsService.getExtensionsRequestById(extensionRequestFullEntity.getId())).thenReturn(extensionRequestFullEntity);
        assertEquals(1, extensionRequestFullEntity.getReasons().size());
        when(extensionRequestsRepository.save(any(ExtensionRequestFullEntity.class))).thenReturn
            (extensionRequestFullEntity);

        reasonsService.removeExtensionsReasonFromRequest(extensionRequestFullEntity.getId(),
            extensionRequestFullEntity.getReasons().get(0).getId());
        verify(extensionRequestsRepository, times(1)).save(captor.capture());
        ExtensionRequestFullEntity extensionRequestResult = captor.getValue();

        assertEquals(0, extensionRequestResult.getReasons().size());
    }

    @Test
    public void canPatchAReason() throws ServiceException {
        ExtensionCreateReason reasonCreate = new ExtensionCreateReason();
        reasonCreate.setAdditionalText("New text");
        reasonCreate.setEndOn(null);

        ExtensionRequestFullEntity requestEntity = new ExtensionRequestFullEntity();
        ExtensionReasonEntity reasonEntity = new ExtensionReasonEntity();
        requestEntity.setId("123");

        reasonEntity.setEndOn(LocalDate.of(2018,1,1));
        reasonEntity.setAdditionalText("Old text");
        reasonEntity.setId("1234");

        requestEntity.addReason(reasonEntity);

        when(requestsService.getExtensionsRequestById("123"))
            .thenReturn(requestEntity);

        reasonsService.patchReason(reasonCreate,"123","1234");

        assertEquals(reasonCreate.getAdditionalText(),
            requestEntity.getReasons().get(0).getAdditionalText());
        verify(extensionRequestsRepository).save(requestEntity);
    }

    @Test
    public void willThrowIfNoReasonExists() {
        ExtensionRequestFullEntity requestEntity = new ExtensionRequestFullEntity();
        requestEntity.setId("123");

        when(requestsService.getExtensionsRequestById("123"))
            .thenReturn(requestEntity);
        try {
            reasonsService.patchReason(new ExtensionCreateReason(), "123", "1234");
            fail();
        } catch(ServiceException ex) {
            assertEquals("Reason id 1234 not found in Request 123", ex.getMessage());
        }
    }
}
