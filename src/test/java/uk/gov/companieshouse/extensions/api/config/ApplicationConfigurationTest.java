package uk.gov.companieshouse.extensions.api.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import org.apache.tika.Tika;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.logging.Logger;

@Tag("UnitTest")
class ApplicationConfigurationTest {

    private ApplicationConfiguration underTest;

    @BeforeEach
    void setUp() {
        underTest = new ApplicationConfiguration();
    }

    @Test
    void restTemplateByteArrayConverter(){
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(RestTemplateConfig.class);
        RestTemplate restTemplate = context.getBean(RestTemplate.class);
        List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();
        ByteArrayHttpMessageConverter byteArrayHttpMessageConverter = converters.stream().filter(ByteArrayHttpMessageConverter.class::isInstance)
                .map(ByteArrayHttpMessageConverter.class::cast)
                .findFirst()
                .orElseThrow(() -> new AssertionError("ByteArrayHttpMessageConverter not found"));

        int  mediaTypesSize = byteArrayHttpMessageConverter.getSupportedMediaTypes().size();
        assertEquals(2, mediaTypesSize);

        boolean isInstance = byteArrayHttpMessageConverter.getSupportedMediaTypes().stream()
                .anyMatch(mediaType -> mediaType.equals(MediaType.APPLICATION_OCTET_STREAM) || mediaType.equals(MediaType.APPLICATION_PDF));
        assertTrue(isInstance, "ByteArrayHttpMessageConverter should support application/octet-stream and application/pdf");
    }

    @Test
    void createLogger() {
        Logger logger = underTest.getLogger();

        assertNotNull(logger);
    }

    @Test
    void createDateTime() {
        Supplier<LocalDateTime> localDateTime = underTest.dateTimeNow();

        assertNotNull(localDateTime.get());
    }

    @Test
    void createRandomUuid() {
        Supplier<String> uuidSupplier = underTest.randomUUID();

        assertNotNull(uuidSupplier.get());
    }

    @Test
    void createTika() {
        Tika tika = underTest.tika();

        assertNotNull(tika);
    }
}
