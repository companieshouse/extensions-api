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
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
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

    @Mock
    private Supplier<LocalDateTime> mockDateTimeSupplierNow;

    private static final LocalDateTime now = LocalDateTime.now();

    @Before
    public void setup() {
        when(mockHttpServletRequest.getRequestURI()).thenReturn(BASE_URL);
        when(mockDateTimeSupplierNow.get()).thenReturn(now);
    }

    @Test
    public void createsExtensionRequestResource() {
        ResponseEntity<ExtensionRequestFull> response =
            controller.createExtensionRequestResource(dummyRequest(), mockHttpServletRequest);

        verify(repo).insert(any(ExtensionRequestFull.class));

        ExtensionRequestFull extensionRequestFull = response.getBody();
        assertEquals(Status.OPEN, extensionRequestFull.getStatus());
        assertEquals(extensionRequestFull.getCreatedOn(), now);
        assertNotNull(extensionRequestFull.getId());
        assertTrue((extensionRequestFull.getId().toString().length() > 0));
        String linkToSelf = extensionRequestFull.getLinks().getLink(() -> "self");
        assertTrue(linkToSelf.startsWith(BASE_URL));
        assertTrue(linkToSelf.length() > BASE_URL.length());
        String headerLinkToSelf = response.getHeaders().getLocation().toString();
        assertTrue(headerLinkToSelf.startsWith(BASE_URL));
        assertTrue(headerLinkToSelf.length() > BASE_URL.length());
        assertEquals(dummyRequest().getAccountingPeriodStartDate(), extensionRequestFull.getAccountingPeriodStartOn());
        assertEquals(dummyRequest().getAccountingPeriodEndDate(), extensionRequestFull.getAccountingPeriodEndOn());
    }

    @Test
    public void canGetExtensionRequestList() {
        final ExtensionRequestFull expectedRequest = new ExtensionRequestFull();
        List<ExtensionRequestFull> response = controller.getExtensionRequestsList();
        response.forEach(request -> {
           assertNotNull(request.getId());
        });
    }

    @Test
    public void canGetSingleExtensionRequest() {
        ExtensionRequestFull expected = new ExtensionRequestFull();
        when(requestsService.getExtensionsRequestById(anyString())).thenReturn(expected);
        ExtensionRequestFull request = controller.getSingleExtensionRequestById("123");
        assertEquals(expected, request);
    }

    @Test
    public void canDeleteExtensionRequest() {
        boolean response = controller.deleteExtensionRequestById("123");
        assertFalse(response);
    }

    public ExtensionCreateRequest dummyRequest() {
        ExtensionCreateRequest request = new ExtensionCreateRequest();
        request.setAccountingPeriodEndDate(LocalDate.of(2018, 12, 12));
        request.setAccountingPeriodStartDate(LocalDate.of(2018, 12, 12));
        return request;
    }

}
