package uk.gov.companieshouse.extensions.api.requests;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
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
    public ExtensionRequest createExtensionRequestResource(@RequestBody ExtensionCreateRequest extensionCreateRequest) {

        ExtensionRequest extensionRequest = new ExtensionRequest();
        extensionRequest.setId(UUID.randomUUID());
        extensionRequest.setUser(extensionCreateRequest.getUser());
        extensionRequest.setStatus(RequestStatus.OPEN);
        extensionRequest.setRequestDate(LocalDateTime.now());
        extensionRequest.setAccountingPeriodStartDate(extensionCreateRequest.getAccountingPeriodStartDate());
        extensionRequest.setAccountingPeriodEndDate(extensionCreateRequest.getAccountingPeriodEndDate());

        extensionRequestsRepository.insert(extensionRequest);

        return extensionRequest;
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
