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

import java.net.URI;

@RestController
@RequestMapping("${api.endpoint.extensions}")
public class ReasonsController {

    @Autowired
    private ReasonsService reasonsService;

    @Autowired
    private ExtensionReasonMapper extensionReasonMapper;

    @PostMapping("/{requestId}/reasons")
    public ResponseEntity<ExtensionReasonDTO> addReasonToRequest(@RequestBody ExtensionCreateReason extensionCreateReason, @PathVariable String requestId) {
      ExtensionReasonEntity extensionReasonEntity = reasonsService.insertExtensionsReason(extensionCreateReason);
      ExtensionReasonDTO extensionReasonDTO = extensionReasonMapper.entityToDTO(extensionReasonEntity);
      return ResponseEntity.created(URI.create("")).body(extensionReasonDTO);
    }

    @DeleteMapping("/{requestId}/reasons/{reasonId}")
    public boolean deleteReasonFromRequest(@PathVariable String requestId, @PathVariable String reasonId) {
      return false;
    }

    @PutMapping("/{requestId}/reasons/{reasonId}")
    public String updateReasonOnRequest(@RequestBody ExtensionCreateReason extensionCreateReason, @PathVariable String requestId, @PathVariable String reasonId) {
      return "ExtensionReason updated: " + extensionCreateReason.toString();
    }
}
