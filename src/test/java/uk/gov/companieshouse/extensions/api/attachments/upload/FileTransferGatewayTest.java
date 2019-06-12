package uk.gov.companieshouse.extensions.api.attachments.upload;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class FileTransferGatewayTest {

    private static final String DUMMY_URL = "http://test";
    private static final String FILE_ID = "12345";
    public static final String EXCEPTION_MESSAGE = "BAD GATEWAY";

    @InjectMocks
    private FileTransferGateway fileTransferGateway;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ApiLogger apiLogger;

    private MultipartFile file;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(fileTransferGateway, "fileTransferApiURL", DUMMY_URL);
        file = new MockMultipartFile("testFile", new byte[10]);
    }

    @Test
    public void testSuccessfulUpload() {
        ResponseEntity<FileTransferApiResponse> apiResponse = apiSuccessResponse();
        when(restTemplate.postForEntity(eq(DUMMY_URL), any(), eq(FileTransferApiResponse.class))).thenReturn(apiResponse);


        FileTransferGatewayResponse response = fileTransferGateway.upload(file);

        assertFalse(response.isInError());
        assertEquals(FILE_ID, response.getFileId());
    }

    @Test
    public void testApiReturnsError() {
        ResponseEntity<FileTransferApiResponse> apiErrorResponse = apiErrorResponse();
        when(restTemplate.postForEntity(eq(DUMMY_URL), any(), eq(FileTransferApiResponse.class))).thenReturn(apiErrorResponse);

        FileTransferGatewayResponse response = fileTransferGateway.upload(file);

        assertTrue(response.isInError());
        assertTrue(StringUtils.isNotBlank(response.getErrorMessage()));
    }

    @Test
    public void testDownload() {
        String fileId = "1234";
        OutputStream outputStream = new ByteArrayOutputStream();
        String downloadUri = DUMMY_URL + "/" + fileId + "/download";
        fileTransferGateway.download(fileId, outputStream);
        verify(restTemplate, times(1)).execute(eq(downloadUri), any(HttpMethod.class), any(RequestCallback.class), any(ResponseExtractor.class));
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
