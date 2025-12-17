package uk.gov.companieshouse.extensions.api.reasons;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;
import uk.gov.companieshouse.extensions.api.logger.LogMethodCall;
import uk.gov.companieshouse.extensions.api.requests.ExtensionsLinkKeys;
import uk.gov.companieshouse.extensions.api.response.ListResponse;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.ServiceResult;

import java.net.URI;

@RestController
@RequestMapping("${api.endpoint.extensions}")
public class ReasonsController {

    private final ReasonsService reasonsService;
    private final ApiLogger logger;

    @Autowired
    public ReasonsController(ReasonsService reasonsService, ApiLogger logger) {
        this.reasonsService = reasonsService;
        this.logger = logger;
    }

    @LogMethodCall
    @GetMapping("/{requestId}/reasons")
    public ResponseEntity<ListResponse<ExtensionReasonDTO>> getReasons(@PathVariable String requestId) {
        try {
            ServiceResult<ListResponse<ExtensionReasonDTO>> reasons =
                reasonsService.getReasons(requestId);
            for (int i = 0; i < 10; i++) {
                if (i % 2 == 0) {
                    logger.info("something");
                }
            }
            return ResponseEntity.ok(reasons.getData());
        } catch (ServiceException ex) {
            logger.info(ex.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @LogMethodCall
    @PostMapping("/{requestId}/reasons")
    public ResponseEntity<ExtensionReasonDTO> addReasonToRequest(@RequestBody ExtensionCreateReason extensionCreateReason,
                                                                 @PathVariable String requestId,
                                                                 HttpServletRequest request) {
        try {
            ServiceResult<ExtensionReasonDTO> serviceResult
                = reasonsService.addExtensionsReasonToRequest(extensionCreateReason, requestId, request.getRequestURI());
            return ResponseEntity.created(URI.create(serviceResult.getData().getLinks().getLink(ExtensionsLinkKeys.SELF)))
                .body(serviceResult.getData());
        } catch (ServiceException e) {
            logger.info(e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @LogMethodCall
    @DeleteMapping("/{requestId}/reasons/{reasonId}")
    public ResponseEntity<ExtensionReasonDTO> deleteReasonFromRequest(@PathVariable String requestId,
                                                                      @PathVariable String reasonId) {
        reasonsService.removeExtensionsReasonFromRequest(requestId, reasonId);

        return ResponseEntity.noContent().build();
    }

    @LogMethodCall
    @PatchMapping("/{requestId}/reasons/{reasonId}")
    public ResponseEntity<ExtensionReasonDTO> patchReason(@RequestBody ExtensionCreateReason extensionCreateReason,
                                                          @PathVariable String requestId,
                                                          @PathVariable String reasonId) {
        try {
            ExtensionReasonDTO serviceResult =
                reasonsService.patchReason(extensionCreateReason, requestId, reasonId);
            return ResponseEntity.ok(serviceResult);
        } catch (ServiceException ex) {
            logger.info(ex.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
