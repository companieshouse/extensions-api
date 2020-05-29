package uk.gov.companieshouse.extensions.api.attachments.file;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.ForwardChainExpectation;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * FileTransferGatewayIntegrationTest with mock server for file-transfer-api
 */
@Tag("Integration")
@ExtendWith(SpringExtension.class)
@SpringBootTest
public class FileTransferGatewayMockIntegrationTest {

    @Autowired
    private FileTransferApiClient gateway;

    private static ClientAndServer mockServer;

    @BeforeAll
    public static void startMockApiServer() {
        mockServer = ClientAndServer.startClientAndServer(8081);
    }

    @AfterAll
    public static void stopMockApiServer() {
        mockServer.stop();
    }

    @BeforeEach
    public void setup() {
        mockServer.reset();
    }

    @Test
    public void willThrowHttpClientExceptionOnUnsupportedMediaType() throws IOException {
        MultipartFile mockFile = new MockMultipartFile("file", "file.txt", "text/plain", "test".getBytes());

        mockServerExpectation("/", "POST")
            .respond(HttpResponse.response()
                .withStatusCode(415));

        HttpClientErrorException thrown = assertThrows(HttpClientErrorException.class, () ->
            gateway.upload(mockFile));
    }

    @Test
    public void willThrowHttpServerExceptionIf500Returned() throws IOException {
        MultipartFile mockFile = new MockMultipartFile("file", "file.txt", "text/plain", "test".getBytes());

        mockServerExpectation("/", "POST")
            .respond(HttpResponse.response()
                .withStatusCode(500));

        HttpServerErrorException thrown = assertThrows(HttpServerErrorException.class, () ->
            gateway.upload(mockFile));
    }

    private ForwardChainExpectation mockServerExpectation(String path, String httpMethod)
            throws IOException {
        return mockServer
            .when(HttpRequest
                .request()
                .withMethod(httpMethod)
                .withPath(path)
                .withKeepAlive(true));
    }
}
