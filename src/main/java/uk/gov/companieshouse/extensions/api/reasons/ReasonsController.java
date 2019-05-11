package uk.gov.companieshouse.extensions.api.reasons;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.rest.response.ChResponseBody;
import uk.gov.companieshouse.service.rest.response.PluggableResponseEntityFactory;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("${api.endpoint.extensions}")
public class ReasonsController {

    private ReasonsService reasonsService;
    private PluggableResponseEntityFactory entityFactory;

    @Autowired
    public ReasonsController(ReasonsService reasonsService,
                             PluggableResponseEntityFactory entityFactory) {
        this.reasonsService = reasonsService;
        this.entityFactory = entityFactory;
    }

    @GetMapping("/{requestId}/reasons")
    public ResponseEntity<ChResponseBody<List<ExtensionReasonDTO>>> getReasons(@PathVariable String requestId) {
        try {
            ServiceResult<List<ExtensionReasonDTO>> reasons = reasonsService.getReasons(requestId);
            return entityFactory.createResponse(reasons);
        } catch(ServiceException ex) {
            return entityFactory.createResponse(ServiceResult.notFound());
        }
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

    @PatchMapping("/{requestId}/reasons/{reasonId}")
    public ResponseEntity<ExtensionReasonDTO> patchReason(@RequestBody ExtensionCreateReason extensionCreateReason,
                                                          @PathVariable String requestId,
                                                          @PathVariable String reasonId) {
      try {
          ExtensionReasonDTO serviceResult =
              reasonsService.patchReason(extensionCreateReason, requestId, reasonId);
          return ResponseEntity.ok(serviceResult);
      } catch(ServiceException ex) {
          return ResponseEntity.notFound().build();
      }
    }
}
