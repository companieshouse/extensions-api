package uk.gov.companieshouse.extensions.api.reasons;

import org.junit.Rule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.companieshouse.extensions.api.attachments.Attachment;
import uk.gov.companieshouse.extensions.api.attachments.file.FileTransferApiClient;
import uk.gov.companieshouse.extensions.api.attachments.file.FileTransferApiClientResponse;
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.REQUEST_ID;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.dummyCreateReason;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.dummyReasonEntity;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.dummyRequestEntity;

@Tag("UnitTest")
@ExtendWith(MockitoExtension.class)
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

        Assertions.assertEquals(2, reasons.getData().getItems().size());
        Assertions.assertEquals(2, reasons.getData().getTotalResults());
        Assertions.assertEquals("reason1", reasons.getData().getItems().get(0).getId());
        Assertions.assertEquals("reason2", reasons.getData().getItems().get(1).getId());
        Assertions.assertEquals(ServiceResultStatus.FOUND, reasons.getStatus());
    }

    @Test
    public void willThrowIfNoRequestExists() {
        ServiceException serviceException = Assertions.assertThrows(ServiceException.class, () -> reasonsService.getReasons("123"));
        Assertions.assertEquals(serviceException.getMessage(), "Extension request 123 not found");
    }

    @Test
    public void willReturnEmptyDataIfNoReasonsInRequest() throws ServiceException {
        ExtensionReasonMapper mapper = new ExtensionReasonMapper();
        ExtensionRequestFullEntity extensionRequestFullEntity = dummyRequestEntity();
        when(requestsService.getExtensionsRequestById(REQUEST_ID)).thenReturn(Optional.of(extensionRequestFullEntity));

        ServiceResult<ListResponse<ExtensionReasonDTO>> reasons =
            reasonsService.getReasons(REQUEST_ID);

        Assertions.assertNotNull(reasons.getData());
        Assertions.assertEquals(0, reasons.getData().getItems().size());
        Assertions.assertEquals(ServiceResultStatus.FOUND, reasons.getStatus());
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

        Assertions.assertNotNull(extensionReasonResult);
        Assertions.assertEquals("string", extensionReasonResult.getReasonInformation());
        Assertions.assertEquals("abc", extensionReasonResult.getId());
        Assertions.assertEquals(dummyCreateReason.getReasonInformation(), extensionReasonResult.getReasonInformation());
        Assertions.assertEquals(dummyCreateReason.getStartOn(), extensionReasonResult.getStartOn());
        Assertions.assertEquals(dummyCreateReason.getEndOn(), extensionReasonResult.getEndOn());
        Assertions.assertEquals(dummyCreateReason.getReason(), extensionReasonResult.getReason());
        Assertions.assertEquals(ReasonStatus.DRAFT, extensionReasonResult.getReasonStatus());

        Links expectedLinks = new Links();
        expectedLinks.setLink(ExtensionsLinkKeys.SELF, "dummyUri/abc");
        Assertions.assertEquals(expectedLinks, result.getData().getLinks());

        Assertions.assertEquals(ServiceResultStatus.CREATED, result.getStatus());
        Assertions.assertNotNull(result.getData());
        Assertions.assertEquals("abc", result.getData().getId());
    }

    @Test
    public void exceptionThrownIfNoRequestFound() {
        when(requestsService.getExtensionsRequestById("123"))
            .thenReturn(Optional.empty());

        ServiceException serviceException = Assertions.assertThrows(ServiceException.class, () -> reasonsService.addExtensionsReasonToRequest(new ExtensionCreateReason(), "123", "url"));
        Assertions.assertEquals(serviceException.getMessage(), "Request 123 not found");
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
        Assertions.assertEquals(1, extensionRequestFullEntity.getReasons().size());

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

        Assertions.assertEquals(0, extensionRequestResult.getReasons().size());
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
        Assertions.assertEquals(1, extensionRequestFullEntity.getReasons().size());
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

        Assertions.assertEquals(0, extensionRequestResult.getReasons().size());
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
        Assertions.assertEquals(1, extensionRequestFullEntity.getReasons().size());
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

        Assertions.assertEquals(0, extensionRequestResult.getReasons().size());
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
        Assertions.assertEquals(1, extensionRequestFullEntity.getReasons().size());
        when(extensionRequestsRepository.save(any(ExtensionRequestFullEntity.class))).thenReturn
            (extensionRequestFullEntity);

        when(fileTransferApiClient.delete("1234")).thenReturn(null);

        reasonsService.removeExtensionsReasonFromRequest(extensionRequestFullEntity.getId(),
            extensionRequestFullEntity.getReasons().get(0).getId());

        verify(fileTransferApiClient).delete("1234");
        verify(logger).error("Unable to delete attachment 1234");

        verify(extensionRequestsRepository, times(1)).save(captor.capture());
        ExtensionRequestFullEntity extensionRequestResult = captor.getValue();

        Assertions.assertEquals(0, extensionRequestResult.getReasons().size());
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
        Assertions.assertEquals(1, extensionRequestFullEntity.getReasons().size());
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

        Assertions.assertEquals(0, extensionRequestResult.getReasons().size());
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
        Assertions.assertEquals(1, extensionRequestFullEntity.getReasons().size());
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

        Assertions.assertEquals(0, extensionRequestResult.getReasons().size());
    }

    @Test
    public void canPatchAReason() throws ServiceException {
        ExtensionCreateReason reasonCreate = new ExtensionCreateReason();
        reasonCreate.setReasonInformation("New text");
        reasonCreate.setEndOn(null);

        ExtensionRequestFullEntity requestEntity = new ExtensionRequestFullEntity();
        ExtensionReasonEntity reasonEntity = new ExtensionReasonEntity();
        requestEntity.setId("123");

        reasonEntity.setEndOn(LocalDate.of(2018, 1, 1));
        reasonEntity.setReasonInformation("Old text");
        reasonEntity.setId("1234");

        requestEntity.addReason(reasonEntity);

        when(requestsService.getExtensionsRequestById("123"))
            .thenReturn(Optional.of(requestEntity));

        reasonsService.patchReason(reasonCreate, "123", "1234");

        Assertions.assertEquals(reasonCreate.getReasonInformation(), requestEntity.getReasons().get(0).getReasonInformation());
        verify(extensionRequestsRepository).save(requestEntity);
    }

    @Test
    public void willThrowIfNoReasonExists() {
        ExtensionRequestFullEntity requestEntity = new ExtensionRequestFullEntity();
        requestEntity.setId("123");

        when(requestsService.getExtensionsRequestById("123"))
            .thenReturn(Optional.of(requestEntity));
        ServiceException serviceException = Assertions.assertThrows(ServiceException.class, () -> reasonsService.patchReason(new ExtensionCreateReason(), "123", "1234"));
        Assertions.assertEquals(serviceException.getMessage(), "Reason id 1234 not found in Request 123");

    }
}
