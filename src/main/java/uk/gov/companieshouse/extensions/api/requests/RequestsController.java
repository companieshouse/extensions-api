package uk.gov.companieshouse.extensions.api.requests;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/company/{companyNumber}/extensions/requests")
public class RequestsController {

    @Autowired
    private RequestsService requestsService;

    @PostMapping("/")
    public String createExtensionRequestResource(@RequestBody ExtensionsRequest request) {
      return "Request received: " + request.toString();
    }

    @GetMapping("/")
    public List<ExtensionsRequest> getExtensionRequestsList() {
        ExtensionsRequest er = new ExtensionsRequest();
        er.setUser("user one");
      return Arrays.asList(er);
    }

    @GetMapping("/{requestId}")
    public ExtensionsRequest getSingleExtensionRequestById(@PathVariable String requestId) {
      ExtensionsRequest extensionsRequest = requestsService.getExtensionsRequestById(requestId);
      return extensionsRequest;
    }

    @DeleteMapping("/{requestId}")
    public boolean deleteExtensionRequestById(@PathVariable String requestId) {
      return false;
    }
}
