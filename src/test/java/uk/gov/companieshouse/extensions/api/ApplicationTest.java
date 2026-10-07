package uk.gov.companieshouse.extensions.api;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.test.web.servlet.client.assertj.RestTestClientResponse;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;
import org.wiremock.spring.EnableWireMock;
import uk.gov.companieshouse.extensions.api.requests.ExtensionCreateRequest;

@EnableWireMock
@ActiveProfiles("test")
@AutoConfigureRestTestClient
@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment =  SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:6.0.19"));

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("management.opentelemetry.enabled", () -> true);
        registry.add("management.opentelemetry.logging.export.max-batch-size", () -> 10);
        registry.add("management.opentelemetry.logging.export.otlp.endpoint", () -> "${wiremock.server.baseUrl}/v1/logs");
        registry.add("management.otlp.metrics.export.url", () -> "${wiremock.server.baseUrl}/v1/metrics");
    }

    @BeforeEach
    void setUp() {
        stubFor(post("/v1/logs").willReturn(aResponse().withStatus(200)));
        stubFor(post("/v1/metrics").willReturn(aResponse().withStatus(200)));
    }

    @Test
    void shouldReceiveRequestAndExportLogs(@Autowired RestTestClient restTestClient) {
        // Given
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 12, 31);

        ExtensionCreateRequest request = new ExtensionCreateRequest();
        request.setAccountingPeriodEndOn(endDate);
        request.setAccountingPeriodStartOn(startDate);

        // When
        RestTestClientResponse postResponse = RestTestClientResponse.from(
            restTestClient.post().uri("/company/12345678/extensions/requests")
                .header("Content-Type", "application/json")
                .body(request)
                .exchange());
        assertThat(postResponse).hasStatus(HttpStatus.CREATED);

        // Then
        RestTestClientResponse getResponse = RestTestClientResponse.from(restTestClient.get().uri("/company/12345678/extensions/requests")
            .header("Content-Type", "application/json")
            .exchange());

        assertThat(getResponse)
            .hasStatus(HttpStatus.OK)
            .bodyJson()
            .hasPathSatisfying("$.items[0].accounting_period_start_on",
                value -> assertThat(value).asString().isEqualTo(startDate.toString()))
            .hasPathSatisfying("$.items[0].accounting_period_end_on",
                value -> assertThat(value).asString().isEqualTo(endDate.toString()));

        verify(postRequestedFor(urlMatching("/v1/logs")));
    }

}
