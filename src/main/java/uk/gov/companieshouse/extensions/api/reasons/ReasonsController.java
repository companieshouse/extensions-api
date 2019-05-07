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
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.rest.response.PluggableResponseEntityFactory;

import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.Response;
import java.net.URI;

@RestController
@RequestMapping("${api.endpoint.extensions}")
public class ReasonsController {

    private ReasonsService reasonsService;

    @Autowired
    public ReasonsController(ReasonsService reasonsService,
                             ExtensionReasonMapper extensionReasonMapper) {
        this.reasonsService = reasonsService;
    }

    @PostMapping("/{requestId}/reasons")
    public ResponseEntity<ExtensionReasonDTO> addReasonToRequest(@RequestBody ExtensionCreateReason extensionCreateReason,
                                                                 @PathVariable String requestId,
                                                                 HttpServletRequest request) {
        try {
            ServiceResult<ExtensionReasonDTO> serviceResult
                = reasonsService.addExtensionsReasonToRequest(extensionCreateReason, requestId, request.getRequestURI());
            return ResponseEntity.created(URI.create(serviceResult.getData().getLinks().getLink
                (() -> "self"))).body(serviceResult.getData());
        } catch(ServiceException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{requestId}/reasons/{reasonId}")
    public ResponseEntity<ExtensionReasonDTO> deleteReasonFromRequest(@PathVariable String requestId,
                                                                      @PathVariable String reasonId) {
        reasonsService.removeExtensionsReasonFromRequest(requestId, reasonId);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{requestId}/reasons/{reasonId}")
    public String updateReasonOnRequest(@RequestBody ExtensionCreateReason extensionCreateReason,
                                        @PathVariable String requestId,
                                        @PathVariable String reasonId) {
      return "ExtensionReason updated: " + extensionCreateReason.toString();
    }
}
