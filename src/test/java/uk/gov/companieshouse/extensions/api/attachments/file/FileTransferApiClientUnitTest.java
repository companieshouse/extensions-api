package uk.gov.companieshouse.extensions.api.attachments.file;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
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
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Tag("UnitTest")
@ExtendWith(MockitoExtension.class)
public class FileTransferApiClientUnitTest {

    private static final String DUMMY_URL = "http://test";
    private static final String FILE_ID = "12345";
    private static final String EXCEPTION_MESSAGE = "BAD THINGS";
    private static final String DOWNLOAD_URI = DUMMY_URL + "/" + FILE_ID + "/download";
    private static final String DELETE_URL = DUMMY_URL + "/" + FILE_ID;

    @Captor
    private ArgumentCaptor<ResponseExtractor<ClientHttpResponse>> responseExtractorArgCaptor;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ApiLogger apiLogger;

    @InjectMocks
    private FileTransferApiClient fileTransferApiClient;

    private MultipartFile file;

    @BeforeEach
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

        Assertions.assertEquals(FILE_ID, fileTransferApiClientResponse.getFileId());
        Assertions.assertEquals(HttpStatus.OK, fileTransferApiClientResponse.getHttpStatus());
    }

    @Test
    public void testUpload_ApiReturnsError() {
        final ResponseEntity<FileTransferApiResponse> apiErrorResponse = apiErrorResponse();

        when(restTemplate.postForEntity(eq(DUMMY_URL), any(), eq(FileTransferApiResponse.class))).thenReturn(apiErrorResponse);

        FileTransferApiClientResponse fileTransferApiClientResponse = fileTransferApiClient.upload(file);

        assertTrue(fileTransferApiClientResponse.getHttpStatus().isError());
        Assertions.assertEquals(apiErrorResponse.getStatusCode(), fileTransferApiClientResponse.getHttpStatus());
        assertTrue(StringUtils.isBlank(fileTransferApiClientResponse.getFileId()));
    }

    @Test
    public void testUpload_GenericExceptionResponse() {
        final RestClientException exception = new RestClientException(EXCEPTION_MESSAGE);

        when(restTemplate.postForEntity(eq(DUMMY_URL), any(), eq(FileTransferApiResponse.class))).thenThrow(exception);


        assertThrows(RestClientException.class, () -> fileTransferApiClient.upload(file));
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
        // Fix the stubbing to accept any string that matches the pattern of DOWNLOAD_URI
        when(restTemplate.execute(eq(DOWNLOAD_URI), eq(HttpMethod.GET), any(RequestCallback.class), ArgumentMatchers.<ResponseExtractor<ClientHttpResponse>>any()))
            .thenReturn(responseFromFileTransferApi);
        when(responseFromFileTransferApi.getBody()).thenReturn(fileInputStream);
        when(responseFromFileTransferApi.getStatusCode()).thenReturn(HttpStatus.OK);
        when(responseFromFileTransferApi.getHeaders()).thenReturn(httpHeaders);

        //do the download
        FileTransferApiClientResponse downloadResponse = fileTransferApiClient.download(FILE_ID, servletResponse);

        //need to capture the responseExtractor lambda passed to the restTemplate so we can test it - this is what actually does the file copy
        verify(restTemplate).execute(eq(DOWNLOAD_URI), eq(HttpMethod.GET), any(RequestCallback.class), responseExtractorArgCaptor.capture());

        //now executing the responseExtractor should cause input stream (file) to be copied to output stream (servletResponse)
        ResponseExtractor<ClientHttpResponse> responseExtractor = responseExtractorArgCaptor.getValue();
        responseExtractor.extractData(responseFromFileTransferApi);

        //check status is ok
        Assertions.assertEquals(HttpStatus.OK, downloadResponse.getHttpStatus());

        //check input stream was copied to output stream when executing the lambda
        assertTrue(ArrayUtils.isEquals(Files.readAllBytes(file.toPath()), servletResponse.getContentAsByteArray()));

        //check headers are correct
        Assertions.assertEquals(contentType.toString(), servletResponse.getHeader("Content-Type"));
        Assertions.assertEquals(String.valueOf(contentLength), servletResponse.getHeader("Content-Length"));
        Assertions.assertEquals(contentDisposition.toString(), servletResponse.getHeader("Content-Disposition"));
    }

    @Test
    public void testDownload_GenericException() {
        final MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        final RestClientException exception = new RestClientException(EXCEPTION_MESSAGE);

        when(restTemplate.execute(eq(DOWNLOAD_URI), eq(HttpMethod.GET), any(RequestCallback.class), ArgumentMatchers.<ResponseExtractor<ClientHttpResponse>>any()))
            .thenThrow(exception);


        RestClientException restClientException = Assertions.assertThrows(RestClientException.class, () -> fileTransferApiClient.download(FILE_ID, servletResponse));
        Assertions.assertEquals(restClientException.getMessage(), exception.getMessage());
    }

    @Test
    public void testDelete_success() {
        final ResponseEntity<String> apiResponse = new ResponseEntity<>("", HttpStatus.NO_CONTENT);

        when(restTemplate.exchange(eq(DELETE_URL), eq(HttpMethod.DELETE), any(), eq(String.class)))
            .thenReturn(apiResponse);

        FileTransferApiClientResponse fileTransferApiClientResponse = fileTransferApiClient.delete(FILE_ID);

        Assertions.assertEquals(HttpStatus.NO_CONTENT, fileTransferApiClientResponse.getHttpStatus());
    }

    @Test
    public void testDelete_ApiReturnsError() {
        final ResponseEntity<String> apiResponse = new ResponseEntity<>("", HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.exchange(eq(DELETE_URL), eq(HttpMethod.DELETE), any(), eq(String.class)))
            .thenReturn(apiResponse);

        FileTransferApiClientResponse fileTransferApiClientResponse = fileTransferApiClient.delete(FILE_ID);

        Assertions.assertTrue(fileTransferApiClientResponse.getHttpStatus().isError());
        Assertions.assertEquals(apiResponse.getStatusCode(), fileTransferApiClientResponse.getHttpStatus());
    }

    @Test
    public void testDelete_GenericExceptionResponse() {
        final RestClientException exception = new RestClientException(EXCEPTION_MESSAGE);

        when(restTemplate.exchange(eq(DELETE_URL), eq(HttpMethod.DELETE), any(), eq(String.class))).thenThrow(exception);
        Exception exceptionThrown = assertThrows(RestClientException.class, () -> {
            fileTransferApiClient.delete(FILE_ID);
        });
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
