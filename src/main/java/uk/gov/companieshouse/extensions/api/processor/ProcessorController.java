package uk.gov.companieshouse.extensions.api.processor;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.endpoint.extensions}")
public class ProcessorController {

    @PostMapping("/{requestId}/status")
    public String updateExtensionRequestStatus(@PathVariable String requestId) {
      Status status = new Status();
      return status.getStatus().toString();
    }
}
