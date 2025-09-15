package uk.gov.companieshouse.extensions.api.attachments.file;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.Callable;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.awaitility.Durations;
import org.jetbrains.annotations.NotNull;
import org.junit.Ignore;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;

/**
 * FileTransferGatewayIntegrationTest
 */
@Tag("CIIntegrationTest")
@RunWith(SpringRunner.class)
@SpringBootTest
@Disabled // Disabled as this test requires a running file-transfer-api instance
@Ignore
public class FileTransferGatewayIntegrationTest {

    @Autowired
    private FileTransferServiceClient gateway;

    @Mock
    private HttpServletResponse mockHttpServletResponse;

    @Value("${http_proxy:}")
    private String envHttpProxy;

    @Value("${https_proxy:}")
    private String envHttpsProxy;

    @PostConstruct
    public void setProxy() {
        setProxySystemProperties("http", envHttpProxy);
        setProxySystemProperties("https", envHttpsProxy);
    }

    private void setProxySystemProperties(String protocol, String envProxyValue) {
        if (StringUtils.isNotBlank(envProxyValue)) {
            final String portSeparator = ":";
            String proxyHost;
            String proxyPort;

            envProxyValue = envProxyValue.replace("http://", "");

            if (envProxyValue.contains(portSeparator)) {
                proxyHost = StringUtils.substringBefore(envProxyValue, portSeparator);
                proxyPort = StringUtils.substringAfter(envProxyValue, portSeparator);
            } else {
                proxyHost = envProxyValue;
                proxyPort = "8080";
            }

            System.setProperty(protocol + ".proxyHost", proxyHost);
            System.setProperty(protocol + ".proxyPort", proxyPort);
        }
    }

    @Test
    public void willUploadDownloadDeleteFile() throws IOException {
        final String filename = "test.png";
        final String fileFolder = "./src/test/resources/input/";
        final String uploadFilePath = fileFolder + filename;
        final String downloadFilePath = fileFolder + "download-" + filename;


        // Upload
        File uploadFile = new File(uploadFilePath);
        FileTransferApiClientResponse uploadResponse = uploadFile(uploadFile);

        assertEquals(HttpStatus.CREATED, uploadResponse.getHttpStatus());
        assertTrue(StringUtils.isNotBlank(uploadResponse.getFileId()));

        String fileID = uploadResponse.getFileId();


        // Download
        File downloadFile = new File(downloadFilePath);
        downloadFile.deleteOnExit();

        FileOutputStream fileOutputStream = new FileOutputStream(downloadFile);

        ServletOutputStream servletOutputStream = getServletOutputStream(fileOutputStream);

        when(mockHttpServletResponse.getOutputStream()).thenReturn(servletOutputStream);

        try {
            System.out.print("Awaiting download...");
            await().atMost(Durations.FIVE_MINUTES)
                .with()
                .pollInterval(Durations.FIVE_SECONDS)
                .until(downloadFile(fileID, mockHttpServletResponse), equalTo(HttpStatus.OK));

        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            fileOutputStream.close();
            System.out.println();
        }

        assertFilesAreEqual(uploadFile, downloadFile);


        // Delete
        System.out.println("Calling delete...");
        FileTransferApiClientResponse deleteResponse = gateway.delete(fileID);

        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getHttpStatus());

        // Try to download after delete - should get 404
        try {
            System.out.println("Calling download...");
            Integer downloadStatus = await().atMost(Durations.ONE_MINUTE)
                .with()
                .pollInterval(Durations.FIVE_SECONDS)
                .until(downloadFile(fileID, mockHttpServletResponse), Objects::nonNull);

            assertEquals(HttpStatus.NOT_FOUND.value(), downloadStatus);
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            fileOutputStream.close();
        }
    }

    @NotNull
    private ServletOutputStream getServletOutputStream(FileOutputStream fileOutputStream) {
        return new ServletOutputStream() {
            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setWriteListener(WriteListener writeListener) {
            }

            @Override
            public void write(int b) throws IOException {
                fileOutputStream.write(b);
            }
        };
    }

    private FileTransferApiClientResponse uploadFile(File uploadFile) throws IOException {
        FileItem fileItem = new DiskFileItem("file", Files.probeContentType(uploadFile.toPath()), false, uploadFile.getName(), (int) uploadFile.length(), uploadFile.getParentFile());
        try {
            IOUtils.copy(new FileInputStream(uploadFile), fileItem.getOutputStream());
        } catch (Exception e) {
            fail(e.getMessage());
        }

        MultipartFile multipartFile = new MockMultipartFile("file.txt", fileItem.get());

        System.out.println("Calling upload...");
        return gateway.upload(multipartFile);
    }

    private Callable<Integer> downloadFile(String fileID, HttpServletResponse httpServletResponse) {
        return () -> {
            int downloadStatus;
            try {
                System.out.print(".");
                gateway.download(fileID, httpServletResponse);
                downloadStatus = httpServletResponse.getStatus();
            } catch (HttpClientErrorException | HttpServerErrorException e) {
                downloadStatus = e.getStatusCode().value();
            }
            return downloadStatus;
        };
    }

    private void assertFilesAreEqual(File uploadFile, File downloadFile) throws IOException {
        try (InputStream inputStream = new FileInputStream(uploadFile);
             InputStream downloadStream = new FileInputStream(downloadFile)) {

            String md5UploadedFile = DigestUtils.md5Hex(inputStream);
            String md5DownloadedFile = DigestUtils.md5Hex(downloadStream);
            assertEquals(md5UploadedFile, md5DownloadedFile);
        }
    }
}
