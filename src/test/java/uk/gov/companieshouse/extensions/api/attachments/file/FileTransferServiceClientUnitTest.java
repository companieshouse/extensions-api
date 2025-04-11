package uk.gov.companieshouse.extensions.api.attachments.file;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

import org.apache.commons.lang.StringUtils;
import org.apache.tika.Tika;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;

import jakarta.servlet.http.HttpServletResponse;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.filetransfer.PrivateFileTransferResourceHandler;
import uk.gov.companieshouse.api.handler.filetransfer.request.PrivateModelFileTransferDelete;
import uk.gov.companieshouse.api.handler.filetransfer.request.PrivateModelFileTransferUpload;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.filetransfer.IdApi;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;

@Tag("UnitTest")
@ExtendWith(MockitoExtension.class)
class FileTransferServiceClientUnitTest {

    private static final String FILE_ID = "12345";
    @Mock
    private Tika tika;

    @Mock
    private ApiLogger apiLogger;

    @Mock
    private InternalApiClient mockClient;

    @Mock
    private Supplier<InternalApiClient> internalApiClientSupplier;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private PrivateFileTransferResourceHandler mockHandler;

    HttpServletResponse servletResponse = new MockHttpServletResponse();

    @InjectMocks
    private FileTransferServiceClient fileTransferServiceClient;

    private MultipartFile file;

    @Test
    void testUpload_success() throws IOException, URIValidationException {
        file = new MockMultipartFile("testFile", new byte[10]);
        when(internalApiClientSupplier.get()).thenReturn(mockClient);
        when(mockClient.privateFileTransferResourceHandler()).thenReturn(mockHandler).thenReturn(mockHandler);
        PrivateModelFileTransferUpload privateModelFileTransferUpload = mock(PrivateModelFileTransferUpload.class);
        IdApi idApi = new IdApi("12345");
        ApiResponse<IdApi> detailsResponse = new ApiResponse<>(200, null, idApi);
        when(mockHandler.upload(any())).thenReturn(privateModelFileTransferUpload);
        when(privateModelFileTransferUpload.execute()).thenReturn(detailsResponse);
        when(tika.detect(any(InputStream.class), any(String.class))).thenReturn("application/pdf");
        FileTransferApiClientResponse fileTransferApiClientResponse = fileTransferServiceClient.upload(file);

        Assertions.assertEquals(FILE_ID, fileTransferApiClientResponse.getFileId());
        Assertions.assertEquals(HttpStatus.OK, fileTransferApiClientResponse.getHttpStatus());
    }

    @Test
    void testUpload_ApiReturnsException() throws IOException, URIValidationException {
        file = new MockMultipartFile("testFile", new byte[10]);
        when(internalApiClientSupplier.get()).thenReturn(mockClient);
        when(mockClient.privateFileTransferResourceHandler()).thenReturn(mockHandler).thenReturn(mockHandler);
        final ResponseEntity<FileTransferApiResponse> apiErrorResponse = apiErrorResponse();

        PrivateModelFileTransferUpload privateModelFileTransferUpload = mock(PrivateModelFileTransferUpload.class);
        ApiResponse<IdApi> detailsResponse = new ApiResponse<>(400, null, null);
        when(mockHandler.upload(any())).thenReturn(privateModelFileTransferUpload);
        when(privateModelFileTransferUpload.execute()).thenReturn(detailsResponse);
        when(tika.detect(any(InputStream.class), any(String.class))).thenReturn("application/pdf");
        FileTransferApiClientResponse fileTransferApiClientResponse = fileTransferServiceClient.upload(file);

        assertTrue(fileTransferApiClientResponse.getHttpStatus().isError());
        Assertions.assertEquals(apiErrorResponse.getStatusCode(), fileTransferApiClientResponse.getHttpStatus());
        assertTrue(StringUtils.isBlank(fileTransferApiClientResponse.getFileId()));
    }

    @Test
    void testUpload_GenericExceptionResponse() throws IOException, URIValidationException {
        file = new MockMultipartFile("testFile", new byte[10]);

        when(internalApiClientSupplier.get()).thenReturn(mockClient);
        when(mockClient.privateFileTransferResourceHandler()).thenReturn(mockHandler).thenReturn(mockHandler);
        PrivateModelFileTransferUpload privateModelFileTransferUpload = mock(PrivateModelFileTransferUpload.class);
        ApiErrorResponseException apiErrorResponseException = createInternalServerError();
        when(tika.detect(any(InputStream.class), any(String.class))).thenReturn("application/pdf");
        when(mockHandler.upload(any())).thenReturn(privateModelFileTransferUpload);
        when(privateModelFileTransferUpload.execute()).thenThrow(apiErrorResponseException);

        assertThrows(HttpServerErrorException.class, () -> fileTransferServiceClient.upload(file));
    }

    @Test
    void testUpload_URIValidationExceptionResponse() throws IOException, URIValidationException {
        file = new MockMultipartFile("testFile", new byte[10]);
        when(internalApiClientSupplier.get()).thenReturn(mockClient);
        when(mockClient.privateFileTransferResourceHandler()).thenReturn(mockHandler).thenReturn(mockHandler);
        PrivateModelFileTransferUpload privateModelFileTransferUpload = mock(PrivateModelFileTransferUpload.class);

        URIValidationException uriValidationException = mock(URIValidationException.class);
        when(tika.detect(any(InputStream.class), any(String.class))).thenReturn("application/pdf");
        when(mockHandler.upload(any())).thenReturn(privateModelFileTransferUpload);
        when(privateModelFileTransferUpload.execute()).thenThrow(uriValidationException);
        FileTransferApiClientResponse fileTransferApiClientResponse = fileTransferServiceClient.upload(file);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, fileTransferApiClientResponse.getHttpStatus());
    }

    @Test
    void testUpload_NullResponse() throws IOException, URIValidationException {
        file = new MockMultipartFile("testFile", new byte[10]);
        when(internalApiClientSupplier.get()).thenReturn(mockClient);
        when(mockClient.privateFileTransferResourceHandler()).thenReturn(mockHandler).thenReturn(mockHandler);
        PrivateModelFileTransferUpload privateModelFileTransferUpload = mock(PrivateModelFileTransferUpload.class);

        when(tika.detect(any(InputStream.class), any(String.class))).thenReturn("application/pdf");
        when(mockHandler.upload(any())).thenReturn(privateModelFileTransferUpload);
        when(privateModelFileTransferUpload.execute()).thenReturn(null);
        FileTransferApiClientResponse fileTransferApiClientResponse = fileTransferServiceClient.upload(file);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, fileTransferApiClientResponse.getHttpStatus());
    }

    @Test
    void testDownload_ResponseIOException() throws IOException, URIValidationException {
        final String contentDispositionType = "attachment";
        final MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
        final String fileName = "file.txt";
        final File inputFile = new File("./src/test/resources/input/test.txt");
        final InputStream fileInputStream = new FileInputStream(inputFile);

        byte[] fileContent = new byte[(int) inputFile.length()];
        fileInputStream.read(fileContent);
        fileInputStream.close();
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

        //create dummy headers that would be returned from calling the file-transfer-api
        ContentDisposition contentDisposition = ContentDisposition.builder(contentDispositionType)
            .filename(fileName).build();

        MultiValueMap<String, String> httpResponseHeaders = new LinkedMultiValueMap<>();
        httpResponseHeaders.add("Content-Length", String.valueOf(fileContent.length));
        httpResponseHeaders.add("Content-Type", contentType.toString());
        httpResponseHeaders.add("Content-Disposition", contentDisposition.toString());

        InputStreamResource inputStreamResource = new InputStreamResource(fileInputStream);

        ResponseEntity<InputStreamResource> a  = new ResponseEntity<InputStreamResource>(inputStreamResource, httpResponseHeaders, HttpStatusCode.valueOf(200));
        when(restTemplate.exchange(eq("/file-transfer-service/12345/downloadbinary"), eq(HttpMethod.GET), any(),eq(InputStreamResource.class))).thenReturn(a);

        fileTransferServiceClient.download(FILE_ID, mockHttpServletResponse);

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), mockHttpServletResponse.getStatus());
    }

    @Test
    void testDownload_success() throws IOException, URIValidationException {
        final String contentDispositionType = "attachment";
        final MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
        final String fileName = "file.txt";
        final File inputFile = new File("./src/test/resources/input/test.txt");
        final InputStream fileInputStream = new FileInputStream(inputFile);

        byte[] fileContent = new byte[(int) inputFile.length()];
        fileInputStream.read(fileContent);
        fileInputStream.close();
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

        //create dummy headers that would be returned from calling the file-transfer-api
        ContentDisposition contentDisposition = ContentDisposition.builder(contentDispositionType)
            .filename(fileName).build();

        MultiValueMap<String, String> httpResponseHeaders = new LinkedMultiValueMap<>();
        httpResponseHeaders.add("Content-Length", String.valueOf(fileContent.length));
        httpResponseHeaders.add("Content-Type", contentType.toString());
        httpResponseHeaders.add("Content-Disposition", contentDisposition.toString());

        FileInputStream inputFile1 = new FileInputStream(inputFile);

        InputStreamResource inputStreamResource = new InputStreamResource(inputFile1);

        ResponseEntity<InputStreamResource> a  = new ResponseEntity<InputStreamResource>(inputStreamResource, httpResponseHeaders, HttpStatusCode.valueOf(200));
        when(restTemplate.exchange(eq("/file-transfer-service/12345/downloadbinary"), eq(HttpMethod.GET), any(),eq(InputStreamResource.class))).thenReturn(a);

        fileTransferServiceClient.download(FILE_ID, mockHttpServletResponse);

        Assertions.assertEquals(HttpStatus.OK.value(), mockHttpServletResponse.getStatus());
        assertEquals(123, mockHttpServletResponse.getContentAsByteArray().length);
        assertArrayEquals(fileContent, mockHttpServletResponse.getContentAsByteArray());

        //check headers are correct
        Assertions.assertEquals(contentType.toString(), mockHttpServletResponse.getHeader("Content-Type"));
        Assertions.assertEquals(String.valueOf(123), mockHttpServletResponse.getHeader("Content-Length"));
        Assertions.assertEquals(contentDisposition.toString(), mockHttpServletResponse.getHeader("Content-Disposition"));
    }

    @Test
    void testDownload_RestClientException() {
        when(restTemplate.exchange(eq("/file-transfer-service/12345/downloadbinary"), eq(HttpMethod.GET), any(),eq(InputStreamResource.class))).thenThrow(RestClientException.class);
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        fileTransferServiceClient.download(FILE_ID, mockHttpServletResponse);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), mockHttpServletResponse.getStatus());
    }

    @Test
    void testDownload_NullResponse() throws ApiErrorResponseException, URIValidationException {
        final MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        when(restTemplate.exchange(eq("/file-transfer-service/12345/downloadbinary"), eq(HttpMethod.GET), any(),eq(InputStreamResource.class))).thenReturn(null);
        fileTransferServiceClient.download(FILE_ID, servletResponse);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), servletResponse.getStatus());
    }

    @Test
    void testDelete_success() throws ApiErrorResponseException, URIValidationException {
        when(internalApiClientSupplier.get()).thenReturn(mockClient);
        when(mockClient.privateFileTransferResourceHandler()).thenReturn(mockHandler).thenReturn(mockHandler);
        PrivateModelFileTransferDelete privateModelFileTransferDelete = mock(PrivateModelFileTransferDelete.class);
        ApiResponse<Void> detailsResponse = new ApiResponse<>(HttpStatus.NO_CONTENT.value(), null);
        when(mockHandler.delete(any())).thenReturn(privateModelFileTransferDelete);
        when(privateModelFileTransferDelete.execute()).thenReturn(detailsResponse);

        FileTransferApiClientResponse fileTransferApiClientResponse = fileTransferServiceClient.delete(FILE_ID);

        Assertions.assertEquals(HttpStatus.NO_CONTENT, fileTransferApiClientResponse.getHttpStatus());
    }

    @Test
    void testDelete_ApiReturnsException() throws ApiErrorResponseException, URIValidationException {
        when(internalApiClientSupplier.get()).thenReturn(mockClient);
        when(mockClient.privateFileTransferResourceHandler()).thenReturn(mockHandler).thenReturn(mockHandler);
        PrivateModelFileTransferDelete privateModelFileTransferDelete = mock(PrivateModelFileTransferDelete.class);
        ApiResponse<Void> exceptionResponse = new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), null);
        when(mockHandler.delete(any())).thenReturn(privateModelFileTransferDelete);
        when(privateModelFileTransferDelete.execute()).thenReturn(exceptionResponse);

        FileTransferApiClientResponse fileTransferApiClientResponse = fileTransferServiceClient.delete(FILE_ID);

        Assertions.assertTrue(fileTransferApiClientResponse.getHttpStatus().isError());
        Assertions.assertEquals(exceptionResponse.getStatusCode(), fileTransferApiClientResponse.getHttpStatus().value());
    }

    @Test
    void testDelete_GenericExceptionResponse() throws ApiErrorResponseException, URIValidationException {
        when(internalApiClientSupplier.get()).thenReturn(mockClient);
        when(mockClient.privateFileTransferResourceHandler()).thenReturn(mockHandler).thenReturn(mockHandler);
        PrivateModelFileTransferDelete privateModelFileTransferDelete = mock(PrivateModelFileTransferDelete.class);
        when(mockHandler.delete(any())).thenReturn(privateModelFileTransferDelete);
        when(privateModelFileTransferDelete.execute()).thenThrow(mock(RuntimeException.class));
        Assertions.assertThrows(RuntimeException.class, () -> fileTransferServiceClient.delete(FILE_ID));
    }

    @Test
    void testDelete_URIValidationException() throws ApiErrorResponseException, URIValidationException {
        when(internalApiClientSupplier.get()).thenReturn(mockClient);
        when(mockClient.privateFileTransferResourceHandler()).thenReturn(mockHandler).thenReturn(mockHandler);
        PrivateModelFileTransferDelete privateModelFileTransferDelete = mock(PrivateModelFileTransferDelete.class);
        when(mockHandler.delete(any())).thenReturn(privateModelFileTransferDelete);
        when(privateModelFileTransferDelete.execute()).thenThrow(mock(URIValidationException.class));
        FileTransferApiClientResponse fileTransferApiClientResponse = fileTransferServiceClient.delete(FILE_ID);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, fileTransferApiClientResponse.getHttpStatus());
    }

    @Test
    void testDelete_ApiErrorResponseException() throws ApiErrorResponseException, URIValidationException {
        when(internalApiClientSupplier.get()).thenReturn(mockClient);
        when(mockClient.privateFileTransferResourceHandler()).thenReturn(mockHandler).thenReturn(mockHandler);
        PrivateModelFileTransferDelete privateModelFileTransferDelete = mock(PrivateModelFileTransferDelete.class);
        when(mockHandler.delete(any())).thenReturn(privateModelFileTransferDelete);
        when(privateModelFileTransferDelete.execute()).thenThrow(createInternalServerError());
        assertThrows(HttpServerErrorException.class, () -> fileTransferServiceClient.delete(FILE_ID));
    }

    @Test
    void testDelete_NullResponse() throws ApiErrorResponseException, URIValidationException {
        when(internalApiClientSupplier.get()).thenReturn(mockClient);
        when(mockClient.privateFileTransferResourceHandler()).thenReturn(mockHandler).thenReturn(mockHandler);
        PrivateModelFileTransferDelete privateModelFileTransferDelete = mock(PrivateModelFileTransferDelete.class);
        when(mockHandler.delete(any())).thenReturn(privateModelFileTransferDelete);
        when(mockHandler.delete(any())).thenReturn(privateModelFileTransferDelete);
        when(privateModelFileTransferDelete.execute()).thenReturn(null);
        FileTransferApiClientResponse fileTransferApiClientResponse = fileTransferServiceClient.delete(FILE_ID);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, fileTransferApiClientResponse.getHttpStatus());
    }
    private ResponseEntity<FileTransferApiResponse> apiErrorResponse() {
        FileTransferApiResponse response = new FileTransferApiResponse();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    static ApiErrorResponseException createInternalServerError() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpResponseException.Builder builder = new HttpResponseException.Builder(
            500,
            "Internal Server Error",
            headers
        );
        builder.setContent("A server error occurred.");
        builder.setMessage("An unexpected error occurred on the server.");
        return new ApiErrorResponseException(builder);
    }
}
