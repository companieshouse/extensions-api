package uk.gov.companieshouse.extensions.api.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        var restTemplate = new RestTemplate();

        restTemplate.getMessageConverters().stream()
            .filter(ByteArrayHttpMessageConverter.class::isInstance)
            .map(ByteArrayHttpMessageConverter.class::cast)
            .forEach(converter -> converter.setSupportedMediaTypes(List.of(
            MediaType.APPLICATION_PDF,
            MediaType.APPLICATION_OCTET_STREAM)));
        return restTemplate;
    }
}
