package uk.gov.companieshouse.extensions.api.requests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.COMPANY_NUMBER;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.REQUEST_ID;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.TESTURI;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.createdBy;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.dummyCreateRequestEntity;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.dummyRequestEntity;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.service.ServiceException;

@Tag("Unit")
@ExtendWith(MockitoExtension.class)
public class RequestServiceUnitTest {

    @InjectMocks
    private RequestsService requestsService;

    @Mock
    private ExtensionRequestsRepository extensionRequestsRepository;

    @Mock
    private Supplier<LocalDateTime> dateTimeSupplierNow;

    @Captor
    private ArgumentCaptor<ExtensionRequestFullEntity> captor;

    @Test
    public void testGetSingleRequest() {
        ExtensionRequestFullEntity entity = dummyRequestEntity();

        when(extensionRequestsRepository.findById(REQUEST_ID)).thenReturn(Optional.of(entity));

        Optional<ExtensionRequestFullEntity> request = requestsService.getExtensionsRequestById(REQUEST_ID);
        assertEquals("id 1234 Acc period start: 2018-12-12  Acc period end: 2019-12-12", request.get().toString());
    }

    @Test
    public void testCorrectDataIsPassedToInsertExtensionRequest() {

        ExtensionCreateRequest extensionCreateRequest = dummyCreateRequestEntity();
        CreatedBy createdBy = createdBy();

        ExtensionRequestFullEntity extensionRequestFullEntity = dummyRequestEntity();
        when(extensionRequestsRepository.insert(any(ExtensionRequestFullEntity.class)))
                .thenReturn(extensionRequestFullEntity);

        requestsService.insertExtensionsRequest(extensionCreateRequest, createdBy, TESTURI, COMPANY_NUMBER);
        verify(extensionRequestsRepository, times(1)).insert(captor.capture());

        ExtensionRequestFullEntity extensionRequestResult = captor.getValue();

        assertNotNull(extensionRequestResult);
        assertEquals(extensionCreateRequest.getAccountingPeriodStartOn(),
                extensionRequestResult.getAccountingPeriodStartOn());
        assertEquals(extensionCreateRequest.getAccountingPeriodEndOn(),
                extensionRequestResult.getAccountingPeriodEndOn());
        assertEquals(Status.OPEN, extensionRequestResult.getStatus());

        CreatedBy createdByInEntity = extensionRequestResult.getCreatedBy();
        assertEquals(createdBy.getEmail(), createdByInEntity.getEmail());
        assertEquals(createdBy.getForename(), createdByInEntity.getForename());
        assertEquals(createdBy.getId(), createdByInEntity.getId());
        assertEquals(createdBy.getSurname(), createdByInEntity.getSurname());
    }

    @Test
    public void willPatchFullRequestEntity() throws ServiceException {
        ExtensionRequestFullEntity extensionRequestFullEntity = new ExtensionRequestFullEntity();
        extensionRequestFullEntity.setStatus(Status.OPEN);
        when(extensionRequestsRepository.findById(anyString()))
            .thenReturn(Optional.of(extensionRequestFullEntity));

        when(extensionRequestsRepository.save(any(ExtensionRequestFullEntity.class)))
            .thenReturn(extensionRequestFullEntity);

        RequestStatus status = new RequestStatus();
        status.setStatus(Status.SUBMITTED);
        ExtensionRequestFullEntity entity = requestsService.patchRequest("request", status);

        verify(extensionRequestsRepository).findById("request");
        verify(extensionRequestsRepository).save(extensionRequestFullEntity);
        assertEquals(Status.SUBMITTED, entity.getStatus());
    }

    @Test
    public void willThrowServiceExceptionIfNoRequest() throws ServiceException {
        ExtensionRequestFullEntity extensionRequestFullEntity = new ExtensionRequestFullEntity();
        extensionRequestFullEntity.setStatus(Status.OPEN);
        when(extensionRequestsRepository.findById(anyString()))
            .thenReturn(Optional.empty());

        RequestStatus status = new RequestStatus();
        status.setStatus(Status.SUBMITTED);

        // expectedException.expectMessage("Request: request1 cannot be found");
        ServiceException thrown =
            assertThrows(ServiceException.class, () -> requestsService.patchRequest("request1", status));
        assertTrue(thrown.getMessage().contains("Request: request1 cannot be found"));
    }
}
