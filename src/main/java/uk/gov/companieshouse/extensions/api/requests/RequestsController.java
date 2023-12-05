package uk.gov.companieshouse.extensions.api.requests;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;
import uk.gov.companieshouse.extensions.api.logger.LogMethodCall;
import uk.gov.companieshouse.extensions.api.response.ListResponse;
import uk.gov.companieshouse.service.ServiceException;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/")
public class RequestsController {

    @Autowired
    private RequestsService requestsService;

    @Autowired
    private ERICHeaderParser ericHeaderParser;

    @Autowired
    private ExtensionRequestMapper extensionRequestMapper;

    @Autowired
    private ApiLogger logger;

    @LogMethodCall
    @PostMapping("${api.endpoint.extensions}/")
    public ResponseEntity<ExtensionRequestFullDTO> createExtensionRequestResource(
        @RequestBody ExtensionCreateRequest extensionCreateRequest, HttpServletRequest request,
        @PathVariable String companyNumber) {

        CreatedBy createdBy = new CreatedBy();
        createdBy.setId(ericHeaderParser.getUserId(request));
        createdBy.setEmail(ericHeaderParser.getEmail(request));
        try {
            createdBy.setForename(ericHeaderParser.getForename(request));
            createdBy.setSurname(ericHeaderParser.getSurname(request));
        } catch (UnsupportedEncodingException ex) {
            logger.debug("Cannot parse username from eric header", request);
            logger.error(ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        String reqUri = request.getRequestURI();

        ExtensionRequestFullEntity extensionRequestFullEntity = requestsService
            .insertExtensionsRequest(extensionCreateRequest, createdBy, reqUri, companyNumber);

        ExtensionRequestFullDTO extensionRequestFullDTO = extensionRequestMapper
            .entityToDTO(extensionRequestFullEntity);

        return ResponseEntity
            .created(URI.create(extensionRequestFullEntity.getLinks().getLink(ExtensionsLinkKeys.SELF)))
            .body(extensionRequestFullDTO);
    }

    @LogMethodCall
    @GetMapping("${api.endpoint.extensions}")
    public ResponseEntity<ListResponse<ExtensionRequestFullDTO>> getExtensionRequestsListByCompanyNumber(
        @PathVariable String companyNumber) {

        List<ExtensionRequestFullDTO> requestFullDTOList = requestsService
            .getExtensionsRequestListByCompanyNumber(companyNumber).stream()
            .map(extensionRequestMapper::entityToDTO).collect(Collectors.toList());

        ListResponse<ExtensionRequestFullDTO> extensionRequestList = ListResponse.<ExtensionRequestFullDTO>builder()
            .withItems(requestFullDTOList).build();
        return ResponseEntity.ok(extensionRequestList);
    }

    @LogMethodCall
    @GetMapping("${api.endpoint.extensions}/{requestId}")
    public ResponseEntity<ExtensionRequestFullEntity> getSingleExtensionRequestById(@PathVariable String requestId) {
        return requestsService.getExtensionsRequestById(requestId).map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @LogMethodCall
    @PatchMapping("${api.endpoint.extensions}/{requestId}")
    public ResponseEntity<ExtensionRequestFullEntity> patchRequest(@PathVariable String requestId,
                                                                   @RequestBody RequestStatus requestStatus) {
        try {
            requestsService.patchRequest(requestId, requestStatus);
            return ResponseEntity.noContent().build();
        } catch (ServiceException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @LogMethodCall
    @DeleteMapping("${api.endpoint.extensions}/{requestId}")
    public boolean deleteExtensionRequestById(@PathVariable String requestId) {
        return false;
    }
}
