package uk.gov.companieshouse.extensions.api.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
@Tag("UnitTest")
public class ApplicationConfigurationTest {


    @Test
    public void restTemplateByteArrayConverter(){

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(RestTemplateConfig.class);
        RestTemplate restTemplate = context.getBean(RestTemplate.class);
        List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();
        ByteArrayHttpMessageConverter byteArrayHttpMessageConverter = converters.stream().filter(ByteArrayHttpMessageConverter.class::isInstance)
                .map(ByteArrayHttpMessageConverter.class::cast)
                .findFirst()
                .orElseThrow(() -> new AssertionError("ByteArrayHttpMessageConverter not found"));

        int  mediaTypesSize = byteArrayHttpMessageConverter.getSupportedMediaTypes().size();
        assertEquals("ByteArrayHttpMessageConverter supported media types size", 2, mediaTypesSize);

        boolean isInstance = byteArrayHttpMessageConverter.getSupportedMediaTypes().stream()
                .anyMatch(mediaType -> mediaType.equals(MediaType.APPLICATION_OCTET_STREAM) || mediaType.equals(MediaType.APPLICATION_PDF));
        assertTrue("ByteArrayHttpMessageConverter should support application/octet-stream and application/pdf", isInstance);
    }

}
