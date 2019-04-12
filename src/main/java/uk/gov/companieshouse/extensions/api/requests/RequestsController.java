package uk.gov.companieshouse.extensions.api.requests;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.service.links.Links;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("${api.endpoint.extensions}")
public class RequestsController {

    @Autowired
    private RequestsService requestsService;

    @Autowired
    private Supplier<LocalDateTime> dateTimeSupplierNow;

    @Autowired
    private ERICHeaderParser ericHeaderParser;

    @PostMapping("/")
    public ResponseEntity<ExtensionRequestFull> createExtensionRequestResource(@RequestBody ExtensionCreateRequest extensionCreateRequest,
                                                                               HttpServletRequest request) {
        UUID uuid = UUID.randomUUID();
        String linkToSelf = request.getRequestURI() + uuid;

        CreatedBy createdBy = new CreatedBy();
        createdBy.setId(ericHeaderParser.getUserId(request));
        createdBy.setEmail(ericHeaderParser.getEmail(request));
        createdBy.setForename(ericHeaderParser.getForename(request));
        createdBy.setSurname(ericHeaderParser.getSurname(request));

        Links links = new Links();
        links.setLink(() ->  "self", linkToSelf);

        Links reasons = new Links();

        ExtensionRequestFull extensionRequestFull = new ExtensionRequestFull();
        extensionRequestFull.setId(uuid);
        extensionRequestFull.setStatus(Status.OPEN);
        extensionRequestFull.setCreatedOn(dateTimeSupplierNow.get());
        extensionRequestFull.setAccountingPeriodStartOn(extensionCreateRequest.getAccountingPeriodStartDate());
        extensionRequestFull.setAccountingPeriodEndOn(extensionCreateRequest.getAccountingPeriodEndDate());
        extensionRequestFull.setLinks(links);
        extensionRequestFull.setCreatedBy(createdBy);
        extensionRequestFull.setReasons(reasons);

        requestsService.insertExtensionsRequest(extensionRequestFull);

        return ResponseEntity.created(URI.create(linkToSelf)).body(extensionRequestFull);
    }

    @GetMapping("/")
    public List<ExtensionRequestFull> getExtensionRequestsList() {
        ExtensionRequestFull er = new ExtensionRequestFull();
        er.setId(UUID.randomUUID());
        return Arrays.asList(er);
    }

    @GetMapping("/{requestId}")
    public ExtensionRequestFull getSingleExtensionRequestById(@PathVariable String requestId) {
        return requestsService.getExtensionsRequestById(requestId);
    }

    @DeleteMapping("/{requestId}")
    public boolean deleteExtensionRequestById(@PathVariable String requestId) {
      return false;
    }
}
