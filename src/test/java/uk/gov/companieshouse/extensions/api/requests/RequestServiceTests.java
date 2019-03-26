package uk.gov.companieshouse.extensions.api.requests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import uk.gov.companieshouse.extensions.api.requests.ExtensionsRequest;
import uk.gov.companieshouse.extensions.api.requests.RequestsService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RequestServiceTests {

    @Autowired
    private RequestsService requestsService;

    @Test
    public void testGetSingleRequest() {
      ExtensionsRequest request = requestsService.getExtensionsRequestById("a1");
      assertEquals("User Joe Bloggs Acc period start: 2018-04-01  Acc period end: 2019-03-31", request.toString());
    }
}
