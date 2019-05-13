package uk.gov.companieshouse.extensions.api.requests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

@RunWith(MockitoJUnitRunner.class)
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
      Optional<ExtensionRequestFullEntity> request =
          requestsService.getExtensionsRequestById(REQUEST_ID);
      assertEquals("id 1234 Acc period start: 2018-12-12  Acc period end: 2019-12-12", request.get()
          .toString());
    }

    @Test
    public void testCorrectDataIsPassedToInsertExtensionRequest() {

        ExtensionCreateRequest extensionCreateRequest = dummyCreateRequestEntity();
        CreatedBy createdBy = createdBy();

        ExtensionRequestFullEntity extensionRequestFullEntity = dummyRequestEntity();
        when(extensionRequestsRepository.insert(any(ExtensionRequestFullEntity.class))).thenReturn(extensionRequestFullEntity);

        requestsService.insertExtensionsRequest(extensionCreateRequest, createdBy, TESTURI);
        verify(extensionRequestsRepository, times(1)).insert(captor.capture());

        ExtensionRequestFullEntity extensionRequestResult = captor.getValue();

        assertNotNull(extensionRequestResult);
        assertEquals(extensionCreateRequest.getAccountingPeriodStartOn(), extensionRequestResult.getAccountingPeriodStartOn());
        assertEquals(extensionCreateRequest.getAccountingPeriodEndOn(), extensionRequestResult.getAccountingPeriodEndOn());
        assertEquals(Status.OPEN, extensionRequestResult.getStatus());

        CreatedBy createdByInEntity = extensionRequestResult.getCreatedBy();
        assertEquals(createdBy.getEmail(), createdByInEntity.getEmail());
        assertEquals(createdBy.getForename(), createdByInEntity.getForename());
        assertEquals(createdBy.getId(), createdByInEntity.getId());
        assertEquals(createdBy.getSurname(), createdByInEntity.getSurname());
    }
}
