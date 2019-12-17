package uk.gov.companieshouse.extensions.api.reasons;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.REQUEST_ID;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.dummyCreateReason;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.dummyReasonEntity;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.dummyRequestEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.companieshouse.extensions.api.attachments.Attachment;
import uk.gov.companieshouse.extensions.api.attachments.file.FileTransferApiClient;
import uk.gov.companieshouse.extensions.api.attachments.file.FileTransferApiClientResponse;
import uk.gov.companieshouse.extensions.api.groups.Unit;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestFullEntity;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestsRepository;
import uk.gov.companieshouse.extensions.api.requests.ExtensionsLinkKeys;
import uk.gov.companieshouse.extensions.api.requests.RequestsService;
import uk.gov.companieshouse.extensions.api.response.ListResponse;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.ServiceResultStatus;
import uk.gov.companieshouse.service.links.Links;

@Category(Unit.class)
@RunWith(MockitoJUnitRunner.class)
public class ReasonServiceUnitTest {

    @InjectMocks
    private ReasonsService reasonsService;

    @Mock
    private RequestsService requestsService;

    @Mock
    private ExtensionReasonMapper reasonMapper;

    @Mock
    private ExtensionRequestsRepository extensionRequestsRepository;

    @Mock
    private Supplier<String> mockRandomUUid;

    @Mock
    private FileTransferApiClient fileTransferApiClient;

    @Mock
    private ApiLogger logger;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Captor
    private ArgumentCaptor<ExtensionRequestFullEntity> captor;

    @Test
    public void canGetListOfReasons() throws ServiceException {
        ExtensionReasonMapper mapper = new ExtensionReasonMapper();
        ExtensionRequestFullEntity extensionRequestFullEntity = dummyRequestEntity();
        ExtensionReasonEntity reason1 = dummyReasonEntity();
        reason1.setId("reason1");
        extensionRequestFullEntity.addReason(reason1);
        ExtensionReasonEntity reason2 = dummyReasonEntity();
        reason2.setId("reason2");
        extensionRequestFullEntity.addReason(reason2);
        when(requestsService.getExtensionsRequestById(REQUEST_ID)).thenReturn(Optional.of(extensionRequestFullEntity));
        when(reasonMapper.entityToDTO(reason1))
            .thenReturn(mapper.entityToDTO(reason1));
        when(reasonMapper.entityToDTO(reason2))
            .thenReturn(mapper.entityToDTO(reason2));

        ServiceResult<ListResponse<ExtensionReasonDTO>> reasons =
            reasonsService.getReasons(REQUEST_ID);

        assertEquals(2, reasons.getData().getItems().size());
        assertEquals(2, reasons.getData().getTotalResults());
        assertEquals("reason1", reasons.getData().getItems().get(0).getId());
        assertEquals("reason2", reasons.getData().getItems().get(1).getId());
        assertEquals(ServiceResultStatus.FOUND, reasons.getStatus());
    }

    @Test
    public void willThrowIfNoRequestExists() throws ServiceException {
        exception.expect(ServiceException.class);
        exception.expectMessage("Extension request 123 not found");

        reasonsService.getReasons("123");
    }

    @Test
    public void willReturnEmptyDataIfNoReasonsInRequest() throws ServiceException {
        ExtensionReasonMapper mapper = new ExtensionReasonMapper();
        ExtensionRequestFullEntity extensionRequestFullEntity = dummyRequestEntity();
        when(requestsService.getExtensionsRequestById(REQUEST_ID)).thenReturn(Optional.of(extensionRequestFullEntity));

        ServiceResult<ListResponse<ExtensionReasonDTO>> reasons =
            reasonsService.getReasons(REQUEST_ID);

        assertNotNull(reasons.getData());
        assertEquals(0, reasons.getData().getItems().size());
        assertEquals(ServiceResultStatus.FOUND, reasons.getStatus());
    }

    @Test
    public void testCorrectDataIsPassedToAddExtensionsReasonToRequest() throws ServiceException {

        ExtensionRequestFullEntity extensionRequestFullEntity = dummyRequestEntity();
        when(requestsService.getExtensionsRequestById(REQUEST_ID)).thenReturn(Optional.of(extensionRequestFullEntity));
        when(extensionRequestsRepository.save(any(ExtensionRequestFullEntity.class)))
            .thenReturn(extensionRequestFullEntity);
        when(mockRandomUUid.get())
            .thenReturn("abc");

        ExtensionCreateReason dummyCreateReason = dummyCreateReason();

        ReasonsService service = new ReasonsService(requestsService, extensionRequestsRepository,
            new ExtensionReasonMapper(), mockRandomUUid, fileTransferApiClient, logger);
        ServiceResult<ExtensionReasonDTO> result =
            service.addExtensionsReasonToRequest(dummyCreateReason,
                REQUEST_ID, "dummyUri");
        verify(extensionRequestsRepository).save(captor.capture());
        verify(mockRandomUUid).get();
        ExtensionRequestFullEntity extensionRequestResult = captor.getValue();
        ExtensionReasonEntity extensionReasonResult = extensionRequestResult.getReasons().get(0);

        assertNotNull(extensionReasonResult);
        assertEquals("string", extensionReasonResult.getReasonInformation());
        assertEquals("abc", extensionReasonResult.getId());
        assertEquals(dummyCreateReason.getReasonInformation(), extensionReasonResult.getReasonInformation());
        assertEquals(dummyCreateReason.getStartOn(), extensionReasonResult.getStartOn());
        assertEquals(dummyCreateReason.getEndOn(), extensionReasonResult.getEndOn());
        assertEquals(dummyCreateReason.getReason(), extensionReasonResult.getReason());
        assertEquals(ReasonStatus.DRAFT, extensionReasonResult.getReasonStatus());

        Links expectedLinks = new Links();
        expectedLinks.setLink(ExtensionsLinkKeys.SELF, "dummyUri/abc");
        assertEquals(expectedLinks, result.getData().getLinks());

        assertEquals(ServiceResultStatus.CREATED, result.getStatus());
        assertNotNull(result.getData());
        assertEquals("abc", result.getData().getId());
    }

    @Test
    public void exceptionThrownIfNoRequestFound() throws ServiceException {
        when(requestsService.getExtensionsRequestById("123"))
            .thenReturn(Optional.empty());

        exception.expect(ServiceException.class);
        exception.expectMessage("Request 123 not found");
        reasonsService.addExtensionsReasonToRequest(new ExtensionCreateReason(), "123", "url");
    }

    @Test
    public void testReasonIsRemovedFromRequest() {
        ExtensionRequestFullEntity extensionRequestFullEntity = dummyRequestEntity();

        Attachment attachment1 = new Attachment();
        attachment1.setId("1234");
        Attachment attachment2 = new Attachment();
        attachment2.setId("5678");

        ExtensionReasonEntity reason = dummyReasonEntity();
        reason.setAttachments(Arrays.asList(attachment1, attachment2));
        extensionRequestFullEntity.addReason(reason);

        when(requestsService.getExtensionsRequestById(extensionRequestFullEntity.getId())).thenReturn(Optional.of(extensionRequestFullEntity));
        assertEquals(1, extensionRequestFullEntity.getReasons().size());

        FileTransferApiClientResponse response = new FileTransferApiClientResponse();
        response.setHttpStatus(HttpStatus.NO_CONTENT);
        when(fileTransferApiClient.delete("1234")).thenReturn(response);
        when(fileTransferApiClient.delete("5678")).thenReturn(response);

        when(extensionRequestsRepository.save(any(ExtensionRequestFullEntity.class))).thenReturn
            (extensionRequestFullEntity);

        reasonsService.removeExtensionsReasonFromRequest(extensionRequestFullEntity.getId(),
            extensionRequestFullEntity.getReasons().get(0).getId());

        verify(fileTransferApiClient).delete("1234");
        verify(fileTransferApiClient).delete("5678");
        verify(extensionRequestsRepository, times(1)).save(captor.capture());
        ExtensionRequestFullEntity extensionRequestResult = captor.getValue();

        assertEquals(0, extensionRequestResult.getReasons().size());
    }

    @Test
    public void testClientErrorIsHandledOnRemoveReason() {
        ExtensionRequestFullEntity extensionRequestFullEntity = dummyRequestEntity();

        Attachment attachment1 = new Attachment();
        attachment1.setId("1234");
        Attachment attachment2 = new Attachment();
        attachment2.setId("5678");

        ExtensionReasonEntity reason = dummyReasonEntity();
        reason.setAttachments(Arrays.asList(attachment1, attachment2));
        extensionRequestFullEntity.addReason(reason);

        when(requestsService.getExtensionsRequestById(extensionRequestFullEntity.getId())).thenReturn(Optional.of(extensionRequestFullEntity));
        assertEquals(1, extensionRequestFullEntity.getReasons().size());
        when(extensionRequestsRepository.save(any(ExtensionRequestFullEntity.class))).thenReturn
            (extensionRequestFullEntity);

        HttpClientErrorException clientException = new HttpClientErrorException(HttpStatus.NOT_FOUND);

        when(fileTransferApiClient.delete("1234")).thenThrow(clientException);
        when(fileTransferApiClient.delete("5678")).thenThrow(clientException);

        reasonsService.removeExtensionsReasonFromRequest(extensionRequestFullEntity.getId(),
            extensionRequestFullEntity.getReasons().get(0).getId());

        verify(fileTransferApiClient).delete("1234");
        verify(logger).error("Unable to delete attachment 1234, status code 404 NOT_FOUND", clientException);
        verify(fileTransferApiClient).delete("5678");
        verify(logger).error("Unable to delete attachment 5678, status code 404 NOT_FOUND", clientException);

        verify(extensionRequestsRepository, times(1)).save(captor.capture());
        ExtensionRequestFullEntity extensionRequestResult = captor.getValue();

        assertEquals(0, extensionRequestResult.getReasons().size());
    }

    @Test
    public void testServerErrorIsHandledOnRemoveReason() {
        ExtensionRequestFullEntity extensionRequestFullEntity = dummyRequestEntity();

        Attachment attachment1 = new Attachment();
        attachment1.setId("1234");
        Attachment attachment2 = new Attachment();
        attachment2.setId("5678");

        ExtensionReasonEntity reason = dummyReasonEntity();
        reason.setAttachments(Arrays.asList(attachment1, attachment2));
        extensionRequestFullEntity.addReason(reason);

        when(requestsService.getExtensionsRequestById(extensionRequestFullEntity.getId())).thenReturn(Optional.of(extensionRequestFullEntity));
        assertEquals(1, extensionRequestFullEntity.getReasons().size());
        when(extensionRequestsRepository.save(any(ExtensionRequestFullEntity.class))).thenReturn
            (extensionRequestFullEntity);

        HttpServerErrorException serverException = new HttpServerErrorException(HttpStatus.NOT_FOUND);

        when(fileTransferApiClient.delete("1234")).thenThrow(serverException);
        when(fileTransferApiClient.delete("5678")).thenThrow(serverException);

        reasonsService.removeExtensionsReasonFromRequest(extensionRequestFullEntity.getId(),
            extensionRequestFullEntity.getReasons().get(0).getId());

        verify(fileTransferApiClient).delete("1234");
        verify(logger).error("Unable to delete attachment 1234, status code 404 NOT_FOUND", serverException);
        verify(fileTransferApiClient).delete("5678");
        verify(logger).error("Unable to delete attachment 1234, status code 404 NOT_FOUND", serverException);

        verify(extensionRequestsRepository, times(1)).save(captor.capture());
        ExtensionRequestFullEntity extensionRequestResult = captor.getValue();

        assertEquals(0, extensionRequestResult.getReasons().size());
    }

    @Test
    public void testNullDeleteResponseIsHandledOnRemoveReason() {
        ExtensionRequestFullEntity extensionRequestFullEntity = dummyRequestEntity();

        Attachment attachment1 = new Attachment();
        attachment1.setId("1234");

        ExtensionReasonEntity reason = dummyReasonEntity();
        reason.setAttachments(Arrays.asList(attachment1));
        extensionRequestFullEntity.addReason(reason);

        when(requestsService.getExtensionsRequestById(extensionRequestFullEntity.getId())).thenReturn(Optional.of(extensionRequestFullEntity));
        assertEquals(1, extensionRequestFullEntity.getReasons().size());
        when(extensionRequestsRepository.save(any(ExtensionRequestFullEntity.class))).thenReturn
            (extensionRequestFullEntity);

        when(fileTransferApiClient.delete("1234")).thenReturn(null);

        reasonsService.removeExtensionsReasonFromRequest(extensionRequestFullEntity.getId(),
            extensionRequestFullEntity.getReasons().get(0).getId());

        verify(fileTransferApiClient).delete("1234");
        verify(logger).error("Unable to delete attachment 1234");

        verify(extensionRequestsRepository, times(1)).save(captor.capture());
        ExtensionRequestFullEntity extensionRequestResult = captor.getValue();

        assertEquals(0, extensionRequestResult.getReasons().size());
    }

    @Test
    public void testDeleteResponseNullHttpStatusIsHandledOnRemoveReason() {
        ExtensionRequestFullEntity extensionRequestFullEntity = dummyRequestEntity();

        Attachment attachment1 = new Attachment();
        attachment1.setId("1234");

        ExtensionReasonEntity reason = dummyReasonEntity();
        reason.setAttachments(Arrays.asList(attachment1));
        extensionRequestFullEntity.addReason(reason);

        when(requestsService.getExtensionsRequestById(extensionRequestFullEntity.getId())).thenReturn(Optional.of(extensionRequestFullEntity));
        assertEquals(1, extensionRequestFullEntity.getReasons().size());
        when(extensionRequestsRepository.save(any(ExtensionRequestFullEntity.class))).thenReturn
            (extensionRequestFullEntity);

        FileTransferApiClientResponse response = new FileTransferApiClientResponse();
        when(fileTransferApiClient.delete("1234")).thenReturn(response);

        reasonsService.removeExtensionsReasonFromRequest(extensionRequestFullEntity.getId(),
            extensionRequestFullEntity.getReasons().get(0).getId());

        verify(fileTransferApiClient).delete("1234");
        verify(logger).error("Unable to delete attachment 1234");

        verify(extensionRequestsRepository, times(1)).save(captor.capture());
        ExtensionRequestFullEntity extensionRequestResult = captor.getValue();

        assertEquals(0, extensionRequestResult.getReasons().size());
    }

    @Test
    public void testDeleteResponseInErrorIsHandledOnRemoveReason() {
        ExtensionRequestFullEntity extensionRequestFullEntity = dummyRequestEntity();

        Attachment attachment1 = new Attachment();
        attachment1.setId("1234");

        ExtensionReasonEntity reason = dummyReasonEntity();
        reason.setAttachments(Arrays.asList(attachment1));
        extensionRequestFullEntity.addReason(reason);

        when(requestsService.getExtensionsRequestById(extensionRequestFullEntity.getId())).thenReturn(Optional.of(extensionRequestFullEntity));
        assertEquals(1, extensionRequestFullEntity.getReasons().size());
        when(extensionRequestsRepository.save(any(ExtensionRequestFullEntity.class))).thenReturn
            (extensionRequestFullEntity);

        FileTransferApiClientResponse response = new FileTransferApiClientResponse();
        response.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        when(fileTransferApiClient.delete("1234")).thenReturn(response);

        reasonsService.removeExtensionsReasonFromRequest(extensionRequestFullEntity.getId(),
            extensionRequestFullEntity.getReasons().get(0).getId());

        verify(fileTransferApiClient).delete("1234");
        verify(logger).error("Unable to delete attachment 1234, status code 500 INTERNAL_SERVER_ERROR");

        verify(extensionRequestsRepository, times(1)).save(captor.capture());
        ExtensionRequestFullEntity extensionRequestResult = captor.getValue();

        assertEquals(0, extensionRequestResult.getReasons().size());
    }

    @Test
    public void canPatchAReason() throws ServiceException {
        ExtensionCreateReason reasonCreate = new ExtensionCreateReason();
        reasonCreate.setReasonInformation("New text");
        reasonCreate.setEndOn(null);

        ExtensionRequestFullEntity requestEntity = new ExtensionRequestFullEntity();
        ExtensionReasonEntity reasonEntity = new ExtensionReasonEntity();
        requestEntity.setId("123");

        reasonEntity.setEndOn(LocalDateTime.of(2018,1,1,0,0,0));
        reasonEntity.setReasonInformation("Old text");
        reasonEntity.setId("1234");

        requestEntity.addReason(reasonEntity);

        when(requestsService.getExtensionsRequestById("123"))
            .thenReturn(Optional.of(requestEntity));

        reasonsService.patchReason(reasonCreate,"123","1234");

        assertEquals(reasonCreate.getReasonInformation(),
            requestEntity.getReasons().get(0).getReasonInformation());
        verify(extensionRequestsRepository).save(requestEntity);
    }

    @Test
    public void willThrowIfNoReasonExists() throws ServiceException {
        ExtensionRequestFullEntity requestEntity = new ExtensionRequestFullEntity();
        requestEntity.setId("123");

        when(requestsService.getExtensionsRequestById("123"))
            .thenReturn(Optional.of(requestEntity));

        exception.expect(ServiceException.class);
        exception.expectMessage("Reason id 1234 not found in Request 123");
        reasonsService.patchReason(new ExtensionCreateReason(), "123", "1234");
    }
}
