package uk.gov.companieshouse.extensions.api.requests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.REQUEST_ID;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.dummyRequestEntity;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import uk.gov.companieshouse.extensions.api.Utils.Utils;

import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class RequestServiceUnitTest {

    @InjectMocks
    private RequestsService requestsService;

    @Mock
    private ExtensionRequestsRepository extensionRequestsRepository;

    @Test
    public void testGetSingleRequest() {
      ExtensionRequestFullEntity entity = dummyRequestEntity();
      when(extensionRequestsRepository.findById(REQUEST_ID)).thenReturn(Optional.of(entity));
      ExtensionRequestFull request = requestsService.getExtensionsRequestById(REQUEST_ID);
      assertEquals("id id Acc period start: 2018-12-12  Acc period end: 2019-12-12", request.toString());
    }

    @Test
    public void testInsertExtensionRequest() {

        ExtensionCreateRequest extensionCreateRequest = Utils.dummyCreateRequestEntity();
        CreatedBy createdBy = Utils.createdBy();

        ExtensionRequestFullEntity extensionRequest = ExtensionRequestFullEntityBuilder
            .newInstance()
            .withCreatedBy(createdBy)
            .withAccountingPeriodStartOn(extensionCreateRequest.getAccountingPeriodStartOn())
            .withAccountingPeriodEndOn(extensionCreateRequest.getAccountingPeriodEndOn())
            .withStatus()
            .build();

        assertNotNull(extensionRequest);
        assertEquals(extensionCreateRequest.getAccountingPeriodStartOn(), extensionRequest.getAccountingPeriodStartOn());
        assertEquals(extensionCreateRequest.getAccountingPeriodEndOn(), extensionRequest.getAccountingPeriodEndOn());

        assertEquals(Status.OPEN, extensionRequest.getStatus());

        CreatedBy createdByInEntity = extensionRequest.getCreatedBy();
        assertEquals(createdBy.getEmail(), createdByInEntity.getEmail());
        assertEquals(createdBy.getForename(), createdByInEntity.getForename());
        assertEquals(createdBy.getId(), createdByInEntity.getId());
        assertEquals(createdBy.getSurname(), createdByInEntity.getSurname());
    }

}
