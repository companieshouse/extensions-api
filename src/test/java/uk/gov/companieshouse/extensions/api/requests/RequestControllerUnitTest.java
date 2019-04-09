package uk.gov.companieshouse.extensions.api.requests;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RequestControllerUnitTest {

    public static final String BASE_URL = "/company/00006400/extensions/requests/";

    @InjectMocks
    private RequestsController controller;

    @Mock
    private ExtensionRequestsRepository repo;

    @Mock
    private RequestsService requestsService;

    @Mock
    private HttpServletRequest mockHttpServletRequest;

    @Before
    public void setup() {
        when(mockHttpServletRequest.getRequestURI()).thenReturn(BASE_URL);
    }

    @Test
    public void createsExtensionRequestResource() {
        ResponseEntity<ExtensionRequest> response =
            controller.createExtensionRequestResource(dummyRequest(), mockHttpServletRequest);
        assertEquals("Micky Mock", response.getBody().getUser());
        assertTrue(response.getBody().getLinks().getLink(() -> "self").startsWith(BASE_URL));
        assertTrue(response.getHeaders().getLocation().toString().startsWith(BASE_URL));
    }

    @Test
    public void canGetExtensionRequestList() {
        final ExtensionRequest expectedRequest = new ExtensionRequest();
        expectedRequest.setUser("user one");
        List<ExtensionRequest> response = controller.getExtensionRequestsList();
        response.forEach(request -> {
           assertEquals(expectedRequest.getUser(), request.getUser());
        });
    }

    @Test
    public void canGetSingleExtensionRequest() {
        ExtensionRequest expected = new ExtensionRequest();
        when(requestsService.getExtensionsRequestById(anyString())).thenReturn(expected);
        ExtensionRequest request = controller.getSingleExtensionRequestById("123");
        assertEquals(expected, request);
    }

    @Test
    public void canDeleteExtensionRequest() {
        boolean response = controller.deleteExtensionRequestById("123");
        assertFalse(response);
    }

    public ExtensionCreateRequest dummyRequest() {
        ExtensionCreateRequest request = new ExtensionCreateRequest();
        request.setUser("Micky Mock");
        request.setAccountingPeriodEndDate(LocalDate.of(2018, 12, 12));
        request.setAccountingPeriodStartDate(LocalDate.of(2018, 12, 12));
        return request;
    }

}
