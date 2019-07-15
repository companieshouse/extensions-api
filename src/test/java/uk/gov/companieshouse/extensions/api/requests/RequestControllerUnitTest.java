package uk.gov.companieshouse.extensions.api.requests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.BASE_URL;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.COMPANY_NUMBER;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.EMAIL;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.FORENAME;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.SURNAME;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.USER_ID;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.dummyRequestDTO;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.dummyRequestEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import uk.gov.companieshouse.extensions.api.attachments.Attachment;
import uk.gov.companieshouse.extensions.api.groups.Unit;
import uk.gov.companieshouse.extensions.api.reasons.ExtensionReasonEntity;
import uk.gov.companieshouse.extensions.api.response.ListResponse;
import uk.gov.companieshouse.service.ServiceException;

@Category(Unit.class)
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

        when(requestsService.insertExtensionsRequest(eq(createRequest), any(CreatedBy.class), eq(requestUri),
                any(String.class))).thenReturn(entity);

        when(mockExtensionRequestMapper.entityToDTO(entity)).thenReturn(entityRequestDTO);

        ResponseEntity<ExtensionRequestFullDTO> response = controller.createExtensionRequestResource(createRequest,
                mockHttpServletRequest, COMPANY_NUMBER);

        verify(requestsService).insertExtensionsRequest(eq(createRequest), any(CreatedBy.class), eq(requestUri),
                any(String.class));

        assertNotNull(entityRequestDTO);
        assertEquals(entityRequestDTO.toString(), Objects.requireNonNull(response.getBody()).toString());
    }

    @Test
    public void canGetExtensionRequestList() {
        ExtensionRequestFullEntity extensionRequestFullEntity = dummyRequestEntity();
        ExtensionRequestFullDTO extensionRequestFullDTO = dummyRequestDTO();
        List<ExtensionRequestFullEntity> extensionRequestFullEntityList = new ArrayList<>();
        extensionRequestFullEntityList.add(extensionRequestFullEntity);

        when(requestsService.getExtensionsRequestListByCompanyNumber(COMPANY_NUMBER))
                .thenReturn(extensionRequestFullEntityList);
        when(mockExtensionRequestMapper.entityToDTO(extensionRequestFullEntity)).thenReturn(extensionRequestFullDTO);

        ResponseEntity<ListResponse<ExtensionRequestFullDTO>> response = controller
                .getExtensionRequestsListByCompanyNumber(COMPANY_NUMBER);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getItems().size());
        assertEquals(extensionRequestFullDTO, response.getBody().getItems().get(0));
    }

    @Test
    public void canGetSingleExtensionRequest() {
        ExtensionRequestFullEntity extensionRequestFullEntity = new ExtensionRequestFullEntity();
        extensionRequestFullEntity.setId("1234");
        ExtensionReasonEntity reasonEntity = new ExtensionReasonEntity();
        reasonEntity.setId("reason1");
        Attachment attachment = new Attachment();
        attachment.setId("attachment1");
        reasonEntity.addAttachment(attachment);
        extensionRequestFullEntity.addReason(reasonEntity);
        when(requestsService.getExtensionsRequestById("1234")).thenReturn(Optional.of(extensionRequestFullEntity));

        ResponseEntity<ExtensionRequestFullEntity> response = controller.getSingleExtensionRequestById("1234");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(extensionRequestFullEntity, response.getBody());
    }

    @Test
    public void canGetSingleExtensionRequest_NotFound() {
        when(requestsService.getExtensionsRequestById("1234")).thenReturn(Optional.ofNullable(null));
        ResponseEntity<ExtensionRequestFullEntity> response = controller.getSingleExtensionRequestById("1234");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void willReturn204WhenAPatchRequestIsSubmitted() throws ServiceException {
        when(requestsService.patchRequest(anyString(), any(RequestStatus.class)))
            .thenReturn(new ExtensionRequestFullEntity());

        RequestStatus status = new RequestStatus();
        status.setStatus(Status.SUBMITTED);
        ResponseEntity<ExtensionRequestFullEntity> response = controller.patchRequest("123", status);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void willReturn404WhenRequestNotFound() throws ServiceException {
        when(requestsService.patchRequest(anyString(), any(RequestStatus.class)))
            .thenThrow(new ServiceException("not found"));

        RequestStatus status = new RequestStatus();
        status.setStatus(Status.SUBMITTED);
        ResponseEntity<ExtensionRequestFullEntity> response = controller.patchRequest("123", status);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
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
