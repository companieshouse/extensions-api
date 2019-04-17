package uk.gov.companieshouse.extensions.api.requests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.companieshouse.extensions.api.Utils.Utils;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RequestServiceUnitTest {

    @Autowired
    private RequestsService requestsService;

    @Test
    public void testGetSingleRequest() {
      ExtensionRequestFull request = requestsService.getExtensionsRequestById("a1");
      assertEquals("Acc period start: 2018-04-01  Acc period end: 2019-03-31", request.toString());
    }

    @Test
    public void testInsertExtensions() {

        ExtensionCreateRequest extensionCreateRequest = Utils.dummyCreateRequestEntity();
        CreatedBy createdBy = Utils.createdBy();
        String reqUri = "http://test";
        ExtensionRequestFullEntity extensionRequest =
            requestsService.insertExtensionsRequest(extensionCreateRequest, createdBy, reqUri);

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
