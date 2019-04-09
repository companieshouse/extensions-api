package uk.gov.companieshouse.extensions.api.requests;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.endpoint.extensions}")
public class RequestsController {

    @Autowired
    private RequestsService requestsService;

    @Autowired
    private ExtensionRequestsRepository extensionRequestsRepository;

    @PostMapping("/")
    public ResponseEntity<String> createExtensionRequestResource(@RequestBody ExtensionRequest request) {
        request.setId(UUID.randomUUID());
        request.setStatus(RequestStatus.OPEN);
        // TODO should request date be date application submitted (on end screen)?
        request.setRequestDate(LocalDateTime.now());

        extensionRequestsRepository.insert(request);

        return ResponseEntity.created(URI.create("//placeholder-uri")).body("Request received: " + request.toString());
    }

    @GetMapping("/")
    public List<ExtensionRequest> getExtensionRequestsList() {
        ExtensionRequest er = new ExtensionRequest();
        er.setUser("user one");
      return Arrays.asList(er);
    }

    @GetMapping("/{requestId}")
    public ExtensionRequest getSingleExtensionRequestById(@PathVariable String requestId) {
        return requestsService.getExtensionsRequestById(requestId);
    }

    @DeleteMapping("/{requestId}")
    public boolean deleteExtensionRequestById(@PathVariable String requestId) {
      return false;
    }
}
