package uk.gov.companieshouse.extensions.api.reasons;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestFullDTO;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestFullEntity;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestMapper;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

@RestController
@RequestMapping("${api.endpoint.extensions}")
public class ReasonsController {

    @Autowired
    private ReasonsService reasonsService;

    @Autowired
    private ExtensionRequestMapper extensionRequestMapper;

    @PostMapping("/{requestId}/reasons")
    public ResponseEntity<ExtensionRequestFullDTO> addReasonToRequest(@RequestBody ExtensionCreateReason extensionCreateReason, @PathVariable String requestId,
                                                                      HttpServletRequest request) {
        ExtensionRequestFullEntity extensionRequestFullEntityUpdated
            = reasonsService.addExtensionsReasonToRequest(extensionCreateReason, requestId, request.getRequestURI());

      ExtensionRequestFullDTO extensionRequestFullDTO = extensionRequestMapper.entityToDTO(extensionRequestFullEntityUpdated);

      return ResponseEntity.created(URI.create(extensionRequestFullEntityUpdated.getLinks().getLink
          (() -> "self"))).body(extensionRequestFullDTO);
    }

    @DeleteMapping("/{requestId}/reasons/{reasonId}")
    public ResponseEntity<ExtensionRequestFullDTO> deleteReasonFromRequest(@PathVariable String
                                                                         requestId, @PathVariable String reasonId) {
        ExtensionRequestFullEntity extensionRequestFullEntity = reasonsService
            .removeExtensionsReasonFromRequest(requestId, reasonId);

        ExtensionRequestFullDTO extensionRequestFullDTO = extensionRequestMapper.entityToDTO(extensionRequestFullEntity);

        return ResponseEntity.created(URI.create(extensionRequestFullEntity.getLinks().getLink
            (() -> "self"))).body(extensionRequestFullDTO);
    }

    @PutMapping("/{requestId}/reasons/{reasonId}")
    public String updateReasonOnRequest(@RequestBody ExtensionCreateReason extensionCreateReason, @PathVariable String requestId, @PathVariable String reasonId) {
      return "ExtensionReason updated: " + extensionCreateReason.toString();
    }
}
