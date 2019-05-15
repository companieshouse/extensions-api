package uk.gov.companieshouse.extensions.api.requests;

import java.net.URI;
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

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("${api.endpoint.extensions}")
public class RequestsController {

    @Autowired
    private RequestsService requestsService;

    @Autowired
    private ERICHeaderParser ericHeaderParser;

    @Autowired
    private ExtensionRequestMapper extensionRequestMapper;

    @PostMapping("/")
    public ResponseEntity<ExtensionRequestFullDTO> createExtensionRequestResource(@RequestBody
                                                                                        ExtensionCreateRequest extensionCreateRequest,
                                                                               HttpServletRequest request) {

        CreatedBy createdBy = new CreatedBy();
        createdBy.setId(ericHeaderParser.getUserId(request));
        createdBy.setEmail(ericHeaderParser.getEmail(request));
        createdBy.setForename(ericHeaderParser.getForename(request));
        createdBy.setSurname(ericHeaderParser.getSurname(request));

         String reqUri = request.getRequestURI();

        ExtensionRequestFullEntity extensionRequestFullEntity = requestsService
            .insertExtensionsRequest
            (extensionCreateRequest, createdBy, reqUri);

        ExtensionRequestFullDTO extensionRequestFullDTO = extensionRequestMapper.entityToDTO
            (extensionRequestFullEntity);

        return ResponseEntity.created(
            URI.create(extensionRequestFullEntity
                .getLinks()
                .getLink(() -> "self")))
            .body(extensionRequestFullDTO);
    }

    @GetMapping("/")
    public List<ExtensionRequestFullDTO> getExtensionRequestsList() {
        ExtensionRequestFullDTO er = new ExtensionRequestFullDTO();
        er.setId(UUID.randomUUID().toString());
        return Arrays.asList(er);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<ExtensionRequestFullEntity> getSingleExtensionRequestById(@PathVariable String
                                                                            requestId) {
        return requestsService.getExtensionsRequestById(requestId)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{requestId}")
    public boolean deleteExtensionRequestById(@PathVariable String requestId) {
      return false;
    }
}
