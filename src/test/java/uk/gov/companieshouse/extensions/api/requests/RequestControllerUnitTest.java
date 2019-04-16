package uk.gov.companieshouse.extensions.api.requests;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.service.links.Links;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.BASE_URL;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.EMAIL;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.FORENAME;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.SURNAME;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.USER_ID;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.dummyRequestDTO;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.dummyRequestEntity;

@RunWith(MockitoJUnitRunner.class)
public class RequestControllerUnitTest {

    @InjectMocks
    private RequestsController controller;

    @Mock
    private RequestsService requestsService;

    @Mock
    private HttpServletRequest mockHttpServletRequest;

    @Mock
    private Supplier<LocalDateTime> mockDateTimeSupplier;

    @Mock
    private ERICHeaderParser mockEricHeaderParser;

    @Mock
    private ExtensionRequestMapper mockExtensionRequestMapper;

    @Before
    public void setup() {
        when(mockHttpServletRequest.getRequestURI()).thenReturn(BASE_URL);
        when(mockEricHeaderParser.getUserId(mockHttpServletRequest)).thenReturn(USER_ID);
        when(mockEricHeaderParser.getEmail(mockHttpServletRequest)).thenReturn(EMAIL);
        when(mockEricHeaderParser.getForename(mockHttpServletRequest)).thenReturn(FORENAME);
        when(mockEricHeaderParser.getSurname(mockHttpServletRequest)).thenReturn(SURNAME);

    }

    @Test
    public void createsExtensionRequestResource() {
        ExtensionCreateRequest createRequest = dummyRequest();
        String requestUri = mockHttpServletRequest.getRequestURI();
        ExtensionRequestFullEntity entity = dummyRequestEntity();
        ExtensionRequestFullDTO entityRequestDTO = dummyRequestDTO();

        when(requestsService.insertExtensionsRequest(eq(createRequest), any(CreatedBy.class),
        eq(requestUri))).thenReturn(entity);

        when(mockExtensionRequestMapper.entityToDTO(entity)).thenReturn(entityRequestDTO);

        ResponseEntity<ExtensionRequestFullDTO> response =
            controller.createExtensionRequestResource(createRequest, mockHttpServletRequest);

        verify(requestsService).insertExtensionsRequest(eq(createRequest), any(CreatedBy.class),
            eq(requestUri));

        assertEquals(entityRequestDTO, response.getBody());
    }

    @Test
    public void canGetExtensionRequestList() {
        final ExtensionRequestFull expectedRequest = new ExtensionRequestFullDTO();
        List<ExtensionRequestFullDTO> response = controller.getExtensionRequestsList();
        response.forEach(request -> {
           assertNotNull(request.getId());
        });
    }

    @Test
    public void canGetSingleExtensionRequest() {
        ExtensionRequestFullEntity expected = new ExtensionRequestFullEntity();
        when(requestsService.getExtensionsRequestById(anyString())).thenReturn(expected);
        ExtensionRequestFull request = controller.getSingleExtensionRequestById("123");
        assertEquals(expected, request);
    }

    @Test
    public void canDeleteExtensionRequest() {
        boolean response = controller.deleteExtensionRequestById("123");
        assertFalse(response);
    }

    private ExtensionCreateRequest dummyRequest() {
        ExtensionCreateRequest request = new ExtensionCreateRequest();
        request.setAccountingPeriodEndOn(LocalDate.of(2019, 12, 12));
        request.setAccountingPeriodStartOn(LocalDate.of(2018, 12, 12));
        return request;
    }
}
