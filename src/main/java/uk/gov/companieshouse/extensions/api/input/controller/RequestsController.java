package uk.gov.companieshouse.extensions.api.input.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.gov.companieshouse.extensions.api.input.dto.ExtensionsRequest;
import uk.gov.companieshouse.extensions.api.input.service.RequestsService;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@RestController
@RequestMapping("/api/extensions/requests")
public class RequestsController {

    private static final Logger LOGGER = LoggerFactory.getLogger("controller.input.api.extensions.ch.gov.uk");

    @Autowired
    private RequestsService requestsService;

    @PostMapping("/")
    public String createExtensionRequestResource(@RequestBody ExtensionsRequest request) {
      String response = "Request received: " + request.toString();
      LOGGER.info(response);
      return response;
    }

    @GetMapping("/")
    public List<ExtensionsRequest> getExtensionRequestsList() {
      return null;
    }

    @GetMapping("/{requestId}")
    public ExtensionsRequest getSingleExtensionRequestById(@PathVariable String requestId) {
      ExtensionsRequest extensionsRequest = requestsService.getExtensionsRequestById(requestId);
      LOGGER.info(extensionsRequest.toString());
      return extensionsRequest;
    }

    @DeleteMapping("/{requestId}")
    public boolean deleteExtensionRequestById(@PathVariable String requestId) {
      boolean result = false;
      Map<String, Object> logData = new HashMap<String, Object>();
      logData.put("Deleted", result);
      LOGGER.infoContext(requestId, "", logData);
      return result;
    }
}
