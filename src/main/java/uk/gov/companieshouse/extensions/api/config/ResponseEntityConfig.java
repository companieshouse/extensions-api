package uk.gov.companieshouse.extensions.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import uk.gov.companieshouse.extensions.api.attachments.FileUploadedResponseFactory;
import uk.gov.companieshouse.service.rest.response.PluggableResponseEntityFactory;

@Configuration
public class ResponseEntityConfig {
	
    @Bean
    public PluggableResponseEntityFactory createResponseFactory() {
        return PluggableResponseEntityFactory
        		.builder()
        		.addStandardFactories()
        		.add(new FileUploadedResponseFactory())
        		.build();
    }
}