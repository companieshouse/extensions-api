package uk.gov.companieshouse.extensions.api.attachments.file;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;

import jakarta.servlet.http.HttpServletResponse;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.filetransfer.FileApi;
import uk.gov.companieshouse.api.filetransfer.IdApi;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.filetransfer.InternalFileTransferClient;
import uk.gov.companieshouse.api.handler.filetransfer.PrivateFileTransferResourceHandler;
import uk.gov.companieshouse.api.handler.filetransfer.request.PrivateFileTransferDelete;
import uk.gov.companieshouse.api.handler.filetransfer.request.PrivateFileTransferDownload;
import uk.gov.companieshouse.api.handler.filetransfer.request.PrivateFileTransferUpload;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;

@Tag("UnitTest")
@ExtendWith(MockitoExtension.class)
class FileTransferServiceClientUnitTest {

    private static final String FILE_ID = "12345";

    @Mock
    private ApiLogger apiLogger;

    @Mock
    private InternalFileTransferClient mockClient;

    @Mock
    private Supplier<InternalFileTransferClient> internalApiClientSupplier;

    @Mock
    private PrivateFileTransferResourceHandler mockHandler;

    @InjectMocks
    private FileTransferServiceClient fileTransferServiceClient;

    private MultipartFile file;

    @BeforeEach
    void setup() {
        file = new MockMultipartFile("testFile", new byte[10]);
        when(internalApiClientSupplier.get()).thenReturn(mockClient);
        when(mockClient.privateFileTransferHandler()).thenReturn(mockHandler).thenReturn(mockHandler);
    }

    @Test
    void testUpload_success() throws IOException, URIValidationException {
        PrivateFileTransferUpload privateFileTransferUpload = mock(PrivateFileTransferUpload.class);
        IdApi idApi = new IdApi(FILE_ID);
        ApiResponse<IdApi> detailsResponse = new ApiResponse<>(200, null, idApi);
        when(mockHandler.upload(any(), any(), any())).thenReturn(privateFileTransferUpload);
        when(privateFileTransferUpload.execute()).thenReturn(detailsResponse);
        FileTransferApiClientResponse fileTransferApiClientResponse = fileTransferServiceClient.upload(file);

        assertEquals(FILE_ID, fileTransferApiClientResponse.getFileId());
        assertEquals(HttpStatus.OK, fileTransferApiClientResponse.getHttpStatus());
    }

    @Test
    void testUpload_ApiReturnsException() throws IOException, URIValidationException {
        final ResponseEntity<FileTransferApiResponse> apiErrorResponse = apiErrorResponse();

        PrivateFileTransferUpload privateFileTransferUpload = mock(PrivateFileTransferUpload.class);
        ApiResponse<IdApi> detailsResponse = new ApiResponse<>(400, null, null);
        when(mockHandler.upload(any(), any(), any())).thenReturn(privateFileTransferUpload);
        when(privateFileTransferUpload.execute()).thenReturn(detailsResponse);
        FileTransferApiClientResponse fileTransferApiClientResponse = fileTransferServiceClient.upload(file);

        assertTrue(fileTransferApiClientResponse.getHttpStatus().isError());
        assertEquals(apiErrorResponse.getStatusCode(), fileTransferApiClientResponse.getHttpStatus());
        assertTrue(StringUtils.isBlank(fileTransferApiClientResponse.getFileId()));
    }

    @Test
    void testUpload_GenericExceptionResponse() throws IOException, URIValidationException {
        PrivateFileTransferUpload privateFileTransferUpload = mock(PrivateFileTransferUpload.class);
        ApiErrorResponseException apiErrorResponseException = createInternalServerError();
        when(mockHandler.upload(any(), any(), any())).thenReturn(privateFileTransferUpload);
        when(privateFileTransferUpload.execute()).thenThrow(apiErrorResponseException);

        assertThrows(HttpServerErrorException.class, () -> fileTransferServiceClient.upload(file));
    }

    @Test
    void testUpload_URIValidationExceptionResponse() throws IOException, URIValidationException {
        PrivateFileTransferUpload privateFileTransferUpload = mock(PrivateFileTransferUpload.class);

        URIValidationException uriValidationException = mock(URIValidationException.class);
        when(mockHandler.upload(any(), any(), any())).thenReturn(privateFileTransferUpload);
        when(privateFileTransferUpload.execute()).thenThrow(uriValidationException);
        assertThrows(FileTransferURIValidationException.class, () -> fileTransferServiceClient.upload(file));
    }

    @Test
    void testUpload_NullResponse() throws IOException, URIValidationException {
        PrivateFileTransferUpload privateFileTransferUpload = mock(PrivateFileTransferUpload.class);

        when(mockHandler.upload(any(), any(), any())).thenReturn(privateFileTransferUpload);
        when(privateFileTransferUpload.execute()).thenReturn(null);
        FileTransferApiClientResponse fileTransferApiClientResponse = fileTransferServiceClient.upload(file);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, fileTransferApiClientResponse.getHttpStatus());
    }

    @Test
    void testDownload_ResponseIOException() throws IOException, URIValidationException {
        final String contentDispositionType = "attachment";
        final int contentLength = 55123;
        final MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
        final String fileName = "file.txt";
        final File inputFile = new File("./src/test/resources/input/test.txt");
        final InputStream fileInputStream = new FileInputStream(inputFile);
        byte[] fileContent = new byte[(int) inputFile.length()];
        fileInputStream.read(fileContent);
        fileInputStream.close();
        PrivateFileTransferDownload privateFileTransferDownload = mock(PrivateFileTransferDownload.class);

        //create dummy headers that would be returned from calling the file-transfer-api
        ContentDisposition contentDisposition = ContentDisposition.builder(contentDispositionType)
            .filename(fileName).build();
        Map<String, Object> httpResponseHeaders = new HashMap<>();
        httpResponseHeaders.put("Content-Length", contentLength);
        httpResponseHeaders.put("Content-Type", contentType);
        httpResponseHeaders.put("Content-Disposition", contentDisposition);
        ApiResponse<FileApi> downloadResponse = new ApiResponse<>(HttpStatus.OK.value(), httpResponseHeaders, new FileApi());

        when(mockHandler.download(any())).thenReturn(privateFileTransferDownload);
        when(privateFileTransferDownload.execute()).thenReturn(downloadResponse);
        HttpServletResponse mockHttpServletResponse = mock(HttpServletResponse.class);

        ArgumentCaptor<Integer> statusCaptor = ArgumentCaptor.forClass(Integer.class);
        when(mockHttpServletResponse.getOutputStream()).thenThrow(IOException.class);
        //do the download
        fileTransferServiceClient.download(FILE_ID, mockHttpServletResponse);
        verify(mockHttpServletResponse).setStatus(statusCaptor.capture());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), statusCaptor.getValue());
    }

    @Test
    void testDownload_success() throws IOException, URIValidationException {
        final String contentDispositionType = "attachment";
        final String fileName = "file.txt";
        PrivateFileTransferDownload privateFileTransferDownload = mock(PrivateFileTransferDownload.class);

        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();

        //create dummy headers that would be returned from calling the file-transfer-api
        ContentDisposition contentDisposition = ContentDisposition.builder(contentDispositionType)
            .filename(fileName).build();
        byte[] fileBinary = new byte[]{1,1,1,1,1,1,1};
        Map<String, Object> httpResponseHeaders = new HashMap<>();
        httpResponseHeaders.put("Content-Length", fileBinary.length);
        httpResponseHeaders.put("Content-Type", MediaType.APPLICATION_OCTET_STREAM);
        httpResponseHeaders.put("Content-Disposition", contentDisposition);
        ApiResponse<FileApi> fileApiApiResponse = new ApiResponse<>(HttpStatus.OK.value(), httpResponseHeaders,
            new FileApi()
                .fileName(fileName)
                .size(fileBinary.length)
                .body(fileBinary));

        when(mockHandler.download(any())).thenReturn(privateFileTransferDownload);
        when(privateFileTransferDownload.execute()).thenReturn(fileApiApiResponse);

        fileTransferServiceClient.download(FILE_ID, mockHttpServletResponse);

        //check status is ok
        assertEquals(HttpStatus.OK.value(), mockHttpServletResponse.getStatus());

        //check input stream was copied to output stream when executing the lambda
        assertArrayEquals(fileBinary, mockHttpServletResponse.getContentAsByteArray());

        //check headers are correct
        assertEquals(MediaType.APPLICATION_OCTET_STREAM_VALUE, mockHttpServletResponse.getHeader("Content-Type"));
        assertEquals(String.valueOf(fileBinary.length), mockHttpServletResponse.getHeader("Content-Length"));
        assertEquals(contentDisposition.toString(), mockHttpServletResponse.getHeader("Content-Disposition"));
    }

    @Test
    void testDownload_GenericException() throws ApiErrorResponseException, URIValidationException {
        PrivateFileTransferDownload privateFileTransferDownload = mock(PrivateFileTransferDownload.class);
        final MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        when(mockHandler.download(any())).thenReturn(privateFileTransferDownload);
        when(privateFileTransferDownload.execute()).thenThrow(mock(RuntimeException.class));
        fileTransferServiceClient.download(FILE_ID, servletResponse);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), servletResponse.getStatus());
    }

    @Test
    void testDownload_ApiErrorResponseException() throws ApiErrorResponseException, URIValidationException {
        PrivateFileTransferDownload privateFileTransferDownload = mock(PrivateFileTransferDownload.class);
        final MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        when(mockHandler.download(any())).thenReturn(privateFileTransferDownload);
        when(privateFileTransferDownload.execute()).thenThrow(createInternalServerError());
        assertThrows(HttpServerErrorException.class, () -> fileTransferServiceClient.download(FILE_ID, servletResponse));
    }

    @Test
    void testDownload_NullResponse() throws ApiErrorResponseException, URIValidationException {
        PrivateFileTransferDownload privateFileTransferDownload = mock(PrivateFileTransferDownload.class);
        final MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        when(mockHandler.download(any())).thenReturn(privateFileTransferDownload);
        when(privateFileTransferDownload.execute()).thenReturn(null);
        fileTransferServiceClient.download(FILE_ID, servletResponse);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), servletResponse.getStatus());
    }

    @Test
    void testDelete_success() throws ApiErrorResponseException, URIValidationException {
        PrivateFileTransferDelete privateFileTransferDelete = mock(PrivateFileTransferDelete.class);
        ApiResponse<Void> detailsResponse = new ApiResponse<>(HttpStatus.NO_CONTENT.value(), null);
        when(mockHandler.delete(any())).thenReturn(privateFileTransferDelete);
        when(privateFileTransferDelete.execute()).thenReturn(detailsResponse);

        FileTransferApiClientResponse fileTransferApiClientResponse = fileTransferServiceClient.delete(FILE_ID);

        assertEquals(HttpStatus.NO_CONTENT, fileTransferApiClientResponse.getHttpStatus());
    }

    @Test
    void testDelete_ApiReturnsException() throws ApiErrorResponseException, URIValidationException {
        PrivateFileTransferDelete privateFileTransferDelete = mock(PrivateFileTransferDelete.class);
        ApiResponse<Void> exceptionResponse = new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), null);
        when(mockHandler.delete(any())).thenReturn(privateFileTransferDelete);
        when(privateFileTransferDelete.execute()).thenReturn(exceptionResponse);

        FileTransferApiClientResponse fileTransferApiClientResponse = fileTransferServiceClient.delete(FILE_ID);

        assertTrue(fileTransferApiClientResponse.getHttpStatus().isError());
        assertEquals(exceptionResponse.getStatusCode(), fileTransferApiClientResponse.getHttpStatus().value());
    }

    @Test
    void testDelete_GenericExceptionResponse() throws ApiErrorResponseException, URIValidationException {
        PrivateFileTransferDelete privateFileTransferDelete = mock(PrivateFileTransferDelete.class);
        when(mockHandler.delete(any())).thenReturn(privateFileTransferDelete);
        when(privateFileTransferDelete.execute()).thenThrow(mock(RuntimeException.class));
        assertThrows(RuntimeException.class, () -> fileTransferServiceClient.delete(FILE_ID));
    }

    @Test
    void testDelete_URIValidationException() throws ApiErrorResponseException, URIValidationException {
        PrivateFileTransferDelete privateFileTransferDelete = mock(PrivateFileTransferDelete.class);
        when(mockHandler.delete(any())).thenReturn(privateFileTransferDelete);
        when(privateFileTransferDelete.execute()).thenThrow(mock(URIValidationException.class));
        FileTransferApiClientResponse fileTransferApiClientResponse = fileTransferServiceClient.delete(FILE_ID);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, fileTransferApiClientResponse.getHttpStatus());
    }

    @Test
    void testDelete_ApiErrorResponseException() throws ApiErrorResponseException, URIValidationException {
        PrivateFileTransferDelete privateFileTransferDelete = mock(PrivateFileTransferDelete.class);
        when(mockHandler.delete(any())).thenReturn(privateFileTransferDelete);
        when(privateFileTransferDelete.execute()).thenThrow(createInternalServerError());
        assertThrows(HttpServerErrorException.class, () -> fileTransferServiceClient.delete(FILE_ID));
    }

    @Test
    void testDelete_NullResponse() throws ApiErrorResponseException, URIValidationException {
        PrivateFileTransferDelete privateFileTransferDelete = mock(PrivateFileTransferDelete.class);
        when(mockHandler.delete(any())).thenReturn(privateFileTransferDelete);
        when(mockHandler.delete(any())).thenReturn(privateFileTransferDelete);
        when(privateFileTransferDelete.execute()).thenReturn(null);
        FileTransferApiClientResponse fileTransferApiClientResponse = fileTransferServiceClient.delete(FILE_ID);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, fileTransferApiClientResponse.getHttpStatus());
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
