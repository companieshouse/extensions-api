package uk.gov.companieshouse.extensions.api.attachments.file;

import org.apache.tika.Tika;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;

import java.io.IOException;
import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Tag("UnitTest")
@ExtendWith(MockitoExtension.class)
class FileTransferServiceClientUploadTest {
    @Mock
    private Tika tika;

    @Mock
    private ApiLogger apiLogger;

    private MultipartFile file;

    @InjectMocks
    private FileTransferServiceClient fileTransferServiceClient;

    @Test
    void testUpload_UnsupportedMediaTypeException() throws IOException {
        file = new MockMultipartFile("testFile", "testFile.rtf", "application/rtf", new byte[10]);
        when(tika.detect(any(InputStream.class), any(String.class))).thenReturn("application/rtf");
        Assertions.assertThrows(HttpClientErrorException.class, () -> fileTransferServiceClient.upload(file));
    }

    @Test
    void testUpload_TikaIOException() throws IOException {
        file = new MockMultipartFile("testFile", "testFile.rtf", "application/rtf", new byte[10]);
        when(tika.detect(any(InputStream.class), any(String.class))).thenThrow(IOException.class);
        FileTransferApiClientResponse fileTransferApiClientResponse = fileTransferServiceClient.upload(file);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, fileTransferApiClientResponse.getHttpStatus());
    }
}
