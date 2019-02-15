package uk.gov.companieshouse.extensions.api.input.controller;

import java.util.Date;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.gov.companieshouse.extensions.api.input.dto.ExtensionsRequest;

@RestController
@RequestMapping("/api/extensions/requests")
public class RequestsController {

    @PostMapping("/")
    public String createExtensionRequestResource(@RequestBody ExtensionsRequest request) {
      return "Request received: " + request.toString();
    }

    @GetMapping("/")
    public List<ExtensionsRequest> getExtensionRequestsList() {
      return null;
    }

    @GetMapping("/{requestId}")
    public ExtensionsRequest getSingleExtensionRequestById(@PathVariable String requestId) {
      ExtensionsRequest extensionsRequest =  new ExtensionsRequest();
      extensionsRequest.setUser("Joe Bloggs");
      extensionsRequest.setAccountingPeriodStartDate(new Date());
      extensionsRequest.setAccountingPeriodEndDate(new Date());
      return extensionsRequest;
    }

    @DeleteMapping("/{requestId}")
    public boolean deleteExtensionRequestById(@PathVariable String requestId) {
      return false;
    }
}
