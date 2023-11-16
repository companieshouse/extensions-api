package uk.gov.companieshouse.extensions.api.attachments.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Optional;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExpectedException;
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
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import uk.gov.companieshouse.extensions.api.groups.Unit;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;

@Category(Unit.class)
@RunWith(MockitoJUnitRunner.class)
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

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

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
    public void testUpload_GenericExceptionResponse() {
        final RestClientException exception = new RestClientException(EXCEPTION_MESSAGE);

        when(restTemplate.postForEntity(eq(DUMMY_URL), any(), eq(FileTransferApiResponse.class))).thenThrow(exception);

        expectedException.expect(RestClientException.class);
        expectedException.expectMessage(exception.getMessage());

        fileTransferApiClient.upload(file);
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
        when(restTemplate.execute(
            ArgumentMatchers.anyString(),
            ArgumentMatchers.eq(HttpMethod.GET),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(),
                Optional.ofNullable(any())))
            .thenAnswer(invocation -> {
                RequestCallback requestCallback = invocation.getArgument(2);
                requestCallback.doWithRequest(null);

                ResponseExtractor<ClientHttpResponse> responseExtractor = invocation.getArgument(3);
                responseExtractor.extractData(responseFromFileTransferApi);

                return "your_response_data"; // The actual response doesn't matter for this test
            });

        when(responseFromFileTransferApi.getBody()).thenReturn(fileInputStream);
        when(responseFromFileTransferApi.getStatusCode()).thenReturn(HttpStatus.OK);
        when(responseFromFileTransferApi.getHeaders()).thenReturn(httpHeaders);

        //do the download
        FileTransferApiClientResponse downloadResponse = fileTransferApiClient.download(FILE_ID, servletResponse);

        //need to capture the responseExtractor lambda passed to the restTemplate so we can test it - this is what actually does the file copy
        verify(restTemplate).execute(eq(DOWNLOAD_URI), eq(HttpMethod.GET), any(), any(), Optional.ofNullable(any()));

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
    public void testDownload_GenericException() {
        final MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        final RestClientException exception = new RestClientException(EXCEPTION_MESSAGE);

        when(restTemplate.execute(eq(DOWNLOAD_URI), eq(HttpMethod.GET), any(RequestCallback.class), ArgumentMatchers.<ResponseExtractor<ClientHttpResponse>>any(), any(FileTransferApiClientResponse.class)))
            .thenThrow(exception);

        expectedException.expect(RestClientException.class);
        expectedException.expectMessage(exception.getMessage());

        fileTransferApiClient.download(FILE_ID, servletResponse);
    }

    @Test
    public void testDelete_success() {
        final ResponseEntity<String> apiResponse = new ResponseEntity<>("", HttpStatus.NO_CONTENT);

        when(restTemplate.exchange(eq(DELETE_URL), eq(HttpMethod.DELETE), any(), eq(String.class)))
            .thenReturn(apiResponse);

        FileTransferApiClientResponse fileTransferApiClientResponse = fileTransferApiClient.delete(FILE_ID);

        assertEquals(HttpStatus.NO_CONTENT, fileTransferApiClientResponse.getHttpStatus());
    }

    @Test
    public void testDelete_ApiReturnsError() {
        final ResponseEntity<String> apiResponse = new ResponseEntity<>("", HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.exchange(eq(DELETE_URL), eq(HttpMethod.DELETE), any(), eq(String.class)))
            .thenReturn(apiResponse);

        FileTransferApiClientResponse fileTransferApiClientResponse = fileTransferApiClient.delete(FILE_ID);

        assertTrue(fileTransferApiClientResponse.getHttpStatus().isError());
        assertEquals(apiResponse.getStatusCode(), fileTransferApiClientResponse.getHttpStatus());
    }

    @Test
    public void testDelete_GenericExceptionResponse() {
        final RestClientException exception = new RestClientException(EXCEPTION_MESSAGE);

        when(restTemplate.exchange(eq(DELETE_URL), eq(HttpMethod.DELETE), any(), eq(String.class))).thenThrow(exception);

        expectedException.expect(RestClientException.class);
        expectedException.expectMessage(exception.getMessage());

        fileTransferApiClient.delete(FILE_ID);
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
