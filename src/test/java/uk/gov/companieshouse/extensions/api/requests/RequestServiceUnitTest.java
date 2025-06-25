package uk.gov.companieshouse.extensions.api.requests;

import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.service.ServiceException;

@Tag("UnitTest")
@ExtendWith(MockitoExtension.class)
class RequestServiceUnitTest {

    @InjectMocks
    private RequestsService requestsService;

    @Mock
    private ExtensionRequestsRepository extensionRequestsRepository;

    @Mock
    private Supplier<LocalDateTime> dateTimeSupplierNow;

    @Captor
    private ArgumentCaptor<ExtensionRequestFullEntity> captor;

    @Test
    void testGetSingleRequest() {
        ExtensionRequestFullEntity entity = dummyRequestEntity();

        when(extensionRequestsRepository.findById(REQUEST_ID)).thenReturn(Optional.of(entity));

        Optional<ExtensionRequestFullEntity> request = requestsService.getExtensionsRequestById(REQUEST_ID);
        Assertions.assertEquals("id 1234 Acc period start: 2018-12-12  Acc period end: 2019-12-12", request.get().toString());
    }

    @Test
    void testCorrectDataIsPassedToInsertExtensionRequest() {
        ExtensionCreateRequest extensionCreateRequest = dummyCreateRequestEntity();
        CreatedBy createdBy = createdBy();

        ExtensionRequestFullEntity extensionRequestFullEntity = dummyRequestEntity();
        when(extensionRequestsRepository.insert(any(ExtensionRequestFullEntity.class)))
            .thenReturn(extensionRequestFullEntity);

        requestsService.insertExtensionsRequest(extensionCreateRequest, createdBy, TESTURI, COMPANY_NUMBER);
        verify(extensionRequestsRepository, times(1)).insert(captor.capture());

        ExtensionRequestFullEntity extensionRequestResult = captor.getValue();

        assertNotNull(extensionRequestResult);
        Assertions.assertEquals(extensionCreateRequest.getAccountingPeriodStartOn(),
            extensionRequestResult.getAccountingPeriodStartOn());
        Assertions.assertEquals(extensionCreateRequest.getAccountingPeriodEndOn(),
            extensionRequestResult.getAccountingPeriodEndOn());
        Assertions.assertEquals(Status.OPEN, extensionRequestResult.getStatus());

        CreatedBy createdByInEntity = extensionRequestResult.getCreatedBy();
        Assertions.assertEquals(createdBy.getEmail(), createdByInEntity.getEmail());
        Assertions.assertEquals(createdBy.getForename(), createdByInEntity.getForename());
        Assertions.assertEquals(createdBy.getId(), createdByInEntity.getId());
        Assertions.assertEquals(createdBy.getSurname(), createdByInEntity.getSurname());
    }

    @Test
    void willPatchFullRequestEntity() throws ServiceException {
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
        Assertions.assertEquals(Status.SUBMITTED, entity.getStatus());
    }

    @Test
    void willCorrectlyPatchFullRequestEntityWhenExtensionRequestIsRejected() throws ServiceException {
        ExtensionRequestFullEntity extensionRequestFullEntity = new ExtensionRequestFullEntity();
        extensionRequestFullEntity.setStatus(Status.OPEN);
        when(extensionRequestsRepository.findById(anyString()))
            .thenReturn(Optional.of(extensionRequestFullEntity));

        when(extensionRequestsRepository.save(any(ExtensionRequestFullEntity.class)))
            .thenReturn(extensionRequestFullEntity);

        RequestStatus status = new RequestStatus();
        status.setStatus(Status.REJECTED_MAX_EXT_LENGTH_EXCEEDED);
        ExtensionRequestFullEntity entity = requestsService.patchRequest("request", status);

        verify(extensionRequestsRepository).findById("request");
        verify(extensionRequestsRepository).save(extensionRequestFullEntity);
        Assertions.assertEquals(Status.REJECTED_MAX_EXT_LENGTH_EXCEEDED, entity.getStatus());
    }

    @Test
    void willThrowServiceExceptionIfNoRequest() {
        ExtensionRequestFullEntity extensionRequestFullEntity = new ExtensionRequestFullEntity();
        extensionRequestFullEntity.setStatus(Status.OPEN);
        when(extensionRequestsRepository.findById(anyString()))
            .thenReturn(Optional.empty());

        RequestStatus status = new RequestStatus();
        status.setStatus(Status.SUBMITTED);

        assertThrows(ServiceException.class, () -> requestsService.patchRequest("request1", status));
    }
}
