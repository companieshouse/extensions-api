package uk.gov.companieshouse.extensions.api.attachments.file;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.extensions.api.groups.Unit;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Category(Unit.class)
@RunWith(MockitoJUnitRunner.class)
public class FileTransferApiClientUnitTest {

    private static final String DUMMY_URL = "http://test";
    private static final String FILE_ID = "12345";
    private static final String EXCEPTION_MESSAGE = "BAD THINGS";
    private static final String DOWNLOAD_URI = DUMMY_URL + "/" + FILE_ID + "/download";

    @Captor
    private ArgumentCaptor<ResponseExtractor<ClientHttpResponse>> responseExtractorArgCaptor;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ApiLogger apiLogger;

    @InjectMocks
    private FileTransferApiClient fileTransferApiClient;

    private MultipartFile file;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(fileTransferApiClient, "fileTransferApiURL", DUMMY_URL);
        file = new MockMultipartFile("testFile", new byte[10]);
    }

    @Test
    public void testUpload_success() {
        final ResponseEntity<FileTransferApiResponse> apiResponse = apiSuccessResponse();

        when(restTemplate.postForEntity(eq(DUMMY_URL), any(), eq(FileTransferApiResponse.class)))
            .thenReturn(apiResponse);

        FileTransferApiClientResponse fileTransferApiClientResponse = fileTransferApiClient.upload(file);

        assertEquals(FILE_ID, fileTransferApiClientResponse.getFileId());
        assertEquals(HttpStatus.OK, fileTransferApiClientResponse.getHttpStatus());
    }

    @Test
    public void testUpload_ApiReturnsError() {
        final ResponseEntity<FileTransferApiResponse> apiErrorResponse = apiErrorResponse();

        when(restTemplate.postForEntity(eq(DUMMY_URL), any(), eq(FileTransferApiResponse.class))).thenReturn(apiErrorResponse);

        FileTransferApiClientResponse fileTransferApiClientResponse = fileTransferApiClient.upload(file);

        assertTrue(fileTransferApiClientResponse.getHttpStatus().isError());
        assertEquals(apiErrorResponse.getStatusCode(), fileTransferApiClientResponse.getHttpStatus());
        assertTrue(StringUtils.isBlank(fileTransferApiClientResponse.getFileId()));
    }

    @Test
    public void testUpload_HttpClientException() {
        final HttpClientErrorException httpClientErrorException = new HttpClientErrorException(HttpStatus.BAD_GATEWAY);

        when(restTemplate.postForEntity(eq(DUMMY_URL), any(), eq(FileTransferApiResponse.class))).thenThrow(httpClientErrorException);

        FileTransferApiClientResponse fileTransferApiClientResponse = fileTransferApiClient.upload(file);

        verify(apiLogger, times(1)).info(httpClientErrorException.getMessage());

        assertTrue(fileTransferApiClientResponse.getHttpStatus().isError());
        assertEquals(httpClientErrorException.getStatusCode(), fileTransferApiClientResponse.getHttpStatus());
        assertTrue(StringUtils.isBlank(fileTransferApiClientResponse.getFileId()));
    }

    @Test
    public void testUpload_HttpServerException() {
        final HttpServerErrorException httpServerErrorException = new HttpServerErrorException(HttpStatus.BAD_GATEWAY);

        when(restTemplate.postForEntity(eq(DUMMY_URL), any(), eq(FileTransferApiResponse.class))).thenThrow(httpServerErrorException);

        FileTransferApiClientResponse fileTransferApiClientResponse = fileTransferApiClient.upload(file);

        verify(apiLogger, times(1)).info(httpServerErrorException.getMessage());

        assertTrue(fileTransferApiClientResponse.getHttpStatus().isError());
        assertEquals(httpServerErrorException.getStatusCode(), fileTransferApiClientResponse.getHttpStatus());
        assertTrue(StringUtils.isBlank(fileTransferApiClientResponse.getFileId()));
    }

    @Test
    public void testUpload_GenericExceptionResponse() {
        final RestClientException exception = new RestClientException(EXCEPTION_MESSAGE);

        when(restTemplate.postForEntity(eq(DUMMY_URL), any(), eq(FileTransferApiResponse.class))).thenThrow(exception);

        FileTransferApiClientResponse fileTransferApiClientResponse = fileTransferApiClient.upload(file);

        assertTrue(fileTransferApiClientResponse.getHttpStatus().isError());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, fileTransferApiClientResponse.getHttpStatus());
        assertTrue(StringUtils.isBlank(fileTransferApiClientResponse.getFileId()));
    }

    @Test
    public void testDownload_success() throws IOException {
//        final String contentDispositionType = "CONTENT_TYPE";
//        final int contentLength = 55123;
//        final MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
//        final String fileName = "file.txt";
//        final byte[] dummyFileBytes = new byte[] {1,0,1,0,0,0,1};
//        final InputStream inputStream = new ByteArrayInputStream(dummyFileBytes);
//        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//
//        ClientHttpResponse clientHttpResponse = Mockito.mock(ClientHttpResponse.class);
//
//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.setContentLength(contentLength);
//        httpHeaders.setContentDisposition(ContentDisposition.builder(contentDispositionType)
//            .filename(fileName).build());
//        httpHeaders.setContentType(contentType);
//
//        when(restTemplate.execute(eq(DOWNLOAD_URI), eq(HttpMethod.GET), any(RequestCallback.class), ArgumentMatchers.<ResponseExtractor<ClientHttpResponse>>any(), any(ApiClientResponse.class)))
//            .thenReturn(clientHttpResponse);
//        when(clientHttpResponse.getBody()).thenReturn(inputStream);
//        when(clientHttpResponse.getStatusCode()).thenReturn(HttpStatus.OK);
//        when(clientHttpResponse.getHeaders()).thenReturn(httpHeaders);
//
//        DownloadResponse downloadResponse = fileTransferApiClient.download(FILE_ID, outputStream);
//
//        //need to capture the responseExtractor lambda passed to the restTemplate so we can test it - this is what actually does the file copy
//        verify(restTemplate).execute(eq(DOWNLOAD_URI), eq(HttpMethod.GET), any(RequestCallback.class), responseExtractorArgCaptor.capture(), any(ApiClientResponse.class));
//        //now executing the lambda should cause input stream to be copied to output stream
//        ResponseExtractor<ClientHttpResponse> lambda = responseExtractorArgCaptor.getValue();
//        lambda.extractData(clientHttpResponse);
//
//        assertEquals(HttpStatus.OK, downloadResponse.getHttpStatus());
//        assertEquals(httpHeaders, downloadResponse.getHttpHeaders());
//        //now check input stream was copied to output stream when executing the lambda
//        final byte[] outputBytes = outputStream.toByteArray();
//        assertTrue(ArrayUtils.isEquals(dummyFileBytes, ArrayUtils.subarray(outputBytes, 0 , dummyFileBytes.length)));
//        assertTrue(ArrayUtils.isEmpty(ArrayUtils.subarray(outputBytes, dummyFileBytes.length, outputBytes.length)));
    }

    @Test
    public void testDownload_HttpClientException() {
//        final OutputStream outputStream = new ByteArrayOutputStream();
//        final HttpClientErrorException httpClientErrorException = new HttpClientErrorException(HttpStatus.BAD_GATEWAY);
//
//        when(restTemplate.execute(eq(DOWNLOAD_URI), eq(HttpMethod.GET), any(RequestCallback.class), ArgumentMatchers.<ResponseExtractor<ClientHttpResponse>>any(), any(ApiClientResponse.class)))
//            .thenThrow(httpClientErrorException);
//
//        DownloadResponse downloadResponse = fileTransferApiClient.download(FILE_ID, outputStream);
//
//        verify(apiLogger, times(1)).info(httpClientErrorException.getMessage());
//
//        assertTrue(downloadResponse.getHttpStatus().isError());
//        assertEquals(httpClientErrorException.getStatusCode(), downloadResponse.getHttpStatus());
//        assertNull(downloadResponse.getHttpHeaders());
    }

    @Test
    public void testDownload_HttpServerException() {
//        final OutputStream outputStream = new ByteArrayOutputStream();
//        final HttpServerErrorException httpServerErrorException = new HttpServerErrorException(HttpStatus.BAD_GATEWAY);
//
//        when(restTemplate.execute(eq(DOWNLOAD_URI), eq(HttpMethod.GET), any(RequestCallback.class), ArgumentMatchers.<ResponseExtractor<ClientHttpResponse>>any(), any(ApiClientResponse.class)))
//            .thenThrow(httpServerErrorException);
//
//        DownloadResponse downloadResponse = fileTransferApiClient.download(FILE_ID, outputStream);
//
//        verify(apiLogger, times(1)).info(httpServerErrorException.getMessage());
//
//        assertTrue(downloadResponse.getHttpStatus().isError());
//        assertEquals(httpServerErrorException.getStatusCode(), downloadResponse.getHttpStatus());
//        assertNull(downloadResponse.getHttpHeaders());
    }

    @Test
    public void testDownload_GenericException() {
//        final OutputStream outputStream = new ByteArrayOutputStream();
//        final RestClientException exception = new RestClientException(EXCEPTION_MESSAGE);
//
//        when(restTemplate.execute(eq(DOWNLOAD_URI), eq(HttpMethod.GET), any(RequestCallback.class), ArgumentMatchers.<ResponseExtractor<ClientHttpResponse>>any(), any(ApiClientResponse.class)))
//            .thenThrow(exception);
//
//        DownloadResponse downloadResponse = fileTransferApiClient.download(FILE_ID, outputStream);
//
//        verify(apiLogger, times(1)).error(exception);
//
//        assertTrue(downloadResponse.getHttpStatus().isError());
//        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, downloadResponse.getHttpStatus());
//        assertNull(downloadResponse.getHttpHeaders());
    }

    private ResponseEntity<FileTransferApiResponse> apiSuccessResponse() {
        FileTransferApiResponse response = new FileTransferApiResponse();
        response.setId(FILE_ID);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private ResponseEntity<FileTransferApiResponse> apiErrorResponse() {
        FileTransferApiResponse response = new FileTransferApiResponse();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
