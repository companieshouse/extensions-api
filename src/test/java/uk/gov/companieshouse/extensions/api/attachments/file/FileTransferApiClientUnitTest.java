package uk.gov.companieshouse.extensions.api.attachments.file;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.extensions.api.groups.Unit;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

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
        final String contentDispositionType = "attachment";
        final int contentLength = 55123;
        final MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
        final String fileName = "file.txt";
        final File file = new File("./src/test/resources/input/test.txt");
        final InputStream fileInputStream = new FileInputStream(file);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        ClientHttpResponse responseFromFileTransferApi = Mockito.mock(ClientHttpResponse.class);

        //create dummy headers that would be returned from calling the file-transfer-api
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentLength(contentLength);
        ContentDisposition contentDisposition = ContentDisposition.builder(contentDispositionType)
            .filename(fileName).build();
        httpHeaders.setContentDisposition(contentDisposition);
        httpHeaders.setContentType(contentType);

        //tell mocks what to return when download method is executed
        when(restTemplate.execute(eq(DOWNLOAD_URI), eq(HttpMethod.GET), any(RequestCallback.class), ArgumentMatchers.<ResponseExtractor<ClientHttpResponse>>any(), any(FileTransferApiClientResponse.class)))
            .thenReturn(responseFromFileTransferApi);
        when(responseFromFileTransferApi.getBody()).thenReturn(fileInputStream);
        when(responseFromFileTransferApi.getStatusCode()).thenReturn(HttpStatus.OK);
        when(responseFromFileTransferApi.getHeaders()).thenReturn(httpHeaders);

        //do the download
        FileTransferApiClientResponse downloadResponse = fileTransferApiClient.download(FILE_ID, servletResponse);

        //need to capture the responseExtractor lambda passed to the restTemplate so we can test it - this is what actually does the file copy
        verify(restTemplate).execute(eq(DOWNLOAD_URI), eq(HttpMethod.GET), any(RequestCallback.class), responseExtractorArgCaptor.capture(), any(FileTransferApiClientResponse.class));

        //now executing the responseExtractor should cause input stream (file) to be copied to output stream (servletResponse)
        ResponseExtractor<ClientHttpResponse> responseExtractor = responseExtractorArgCaptor.getValue();
        responseExtractor.extractData(responseFromFileTransferApi);

        //check status is ok
        assertEquals(HttpStatus.OK, downloadResponse.getHttpStatus());

        //check input stream was copied to output stream when executing the lambda
        assertTrue(ArrayUtils.isEquals(Files.readAllBytes(file.toPath()), servletResponse.getContentAsByteArray()));

        //check headers are correct
        assertEquals(contentType.toString(), servletResponse.getHeader("Content-Type"));
        assertEquals(String.valueOf(contentLength), servletResponse.getHeader("Content-Length"));
        assertEquals(contentDisposition.toString(), servletResponse.getHeader("Content-Disposition"));
    }

    @Test
    public void testDownload_HttpClientException() {
        final MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        final HttpClientErrorException httpClientErrorException = new HttpClientErrorException(HttpStatus.BAD_GATEWAY);

        when(restTemplate.execute(eq(DOWNLOAD_URI), eq(HttpMethod.GET), any(RequestCallback.class), ArgumentMatchers.<ResponseExtractor<ClientHttpResponse>>any(), any(FileTransferApiClientResponse.class)))
            .thenThrow(httpClientErrorException);

        FileTransferApiClientResponse downloadResponse = fileTransferApiClient.download(FILE_ID, servletResponse);

        verify(apiLogger, times(1)).info(httpClientErrorException.getMessage());

        assertTrue(downloadResponse.getHttpStatus().isError());
        assertEquals(httpClientErrorException.getStatusCode(), downloadResponse.getHttpStatus());

    }

    @Test
    public void testDownload_HttpServerException() {
        final MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        final HttpServerErrorException httpServerErrorException = new HttpServerErrorException(HttpStatus.BAD_GATEWAY);

        when(restTemplate.execute(eq(DOWNLOAD_URI), eq(HttpMethod.GET), any(RequestCallback.class), ArgumentMatchers.<ResponseExtractor<ClientHttpResponse>>any(), any(FileTransferApiClientResponse.class)))
            .thenThrow(httpServerErrorException);

        FileTransferApiClientResponse downloadResponse = fileTransferApiClient.download(FILE_ID, servletResponse);

        verify(apiLogger, times(1)).info(httpServerErrorException.getMessage());

        assertTrue(downloadResponse.getHttpStatus().isError());
        assertEquals(httpServerErrorException.getStatusCode(), downloadResponse.getHttpStatus());
    }

    @Test
    public void testDownload_GenericException() {
        final MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        final RestClientException exception = new RestClientException(EXCEPTION_MESSAGE);

        when(restTemplate.execute(eq(DOWNLOAD_URI), eq(HttpMethod.GET), any(RequestCallback.class), ArgumentMatchers.<ResponseExtractor<ClientHttpResponse>>any(), any(FileTransferApiClientResponse.class)))
            .thenThrow(exception);

        FileTransferApiClientResponse downloadResponse = fileTransferApiClient.download(FILE_ID, servletResponse);

        verify(apiLogger, times(1)).error(exception);

        assertTrue(downloadResponse.getHttpStatus().isError());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, downloadResponse.getHttpStatus());
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
