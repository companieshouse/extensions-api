package uk.gov.companieshouse.extensions.api.processor.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.gov.companieshouse.extensions.api.processor.dto.Status;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@RestController
@RequestMapping("/api/extensions/processor")
public class ProcessorController {

    private static final Logger LOGGER = LoggerFactory.getLogger("controller.processor.api.extensions.ch.gov.uk");

    @PostMapping("/{requestId}/status")
    public String updateExtensionRequestStatus(@PathVariable String requestId) {
      Status status = new Status();
      LOGGER.info(status.getStatus().toString());
      return status.getStatus().toString();
    }
}
