package uk.gov.companieshouse.extensions.api.attachments.file;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.ForwardChainExpectation;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

/**
 * FileTransferGatewayIntegrationTest with mock server for file-transfer-api
 */
@Tag("IntegrationTest")
@TestPropertySource(properties = {"EXTENSIONS_API_MONGODB_URL=mongodb://mongo-db1-toro1.development.aws.internal:27017", "server.port=8093",
    "api.endpoint.extensions=/company/{companyNumber}/extensions/requests",
    "spring.data.mongodb.uri=mongodb://mongo-db1-toro1.development.aws.internal:27017/extension_requests",
    "FILE_TRANSFER_API_URL=http://localhost:8081/",
    "FILE_TRANSFER_API_KEY=12345",
    "MONGO_CONNECTION_POOL_MIN_SIZE=0",
    "MONGO_CONNECTION_MAX_IDLE_TIME=0",
    "MONGO_CONNECTION_MAX_LIFE_TIME=0",
    "spring.servlet.multipart.max-file-size=100",
    "spring.servlet.multipart.max-request-size=200"})
@ExtendWith(MockServerExtension.class)
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
            .respond(response()
                .withStatusCode(415));
        Assertions.assertThrows(HttpClientErrorException.class, () -> gateway.upload(mockFile));
    }

    @Test
    public void willThrowHttpServerExceptionIf500Returned() throws IOException {
        MultipartFile mockFile = new MockMultipartFile("file", "file.txt", "text/plain", "test".getBytes());

        mockServerExpectation("/", "POST")
            .respond(response()
                .withStatusCode(500));
        Assertions.assertThrows(HttpServerErrorException.class, () -> gateway.upload(mockFile));

    }

    private ForwardChainExpectation mockServerExpectation(String path, String httpMethod)
        throws IOException {
        return mockServer
            .when(request()
                .withMethod(httpMethod)
                .withPath(path)
                .withKeepAlive(true));

    }
}
