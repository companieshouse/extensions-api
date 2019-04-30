package uk.gov.companieshouse.extensions.api.attachments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.extensions.api.requests.CreatedBy;
import uk.gov.companieshouse.extensions.api.requests.ERICHeaderParser;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.rest.err.Err;
import uk.gov.companieshouse.service.rest.err.Errors;
import uk.gov.companieshouse.service.rest.response.ChResponseBody;
import uk.gov.companieshouse.service.rest.response.PluggableResponseEntityFactory;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/company/{companyNumber}/extensions/requests")
public class AttachmentsController {

    private PluggableResponseEntityFactory responseEntityFactory;
    private AttachmentsService attachmentsService;

    @Autowired
    public AttachmentsController(PluggableResponseEntityFactory responseEntityFactory,
                                 AttachmentsService attachmentsService) {
        this.responseEntityFactory = responseEntityFactory;
        this.attachmentsService = attachmentsService;
    }

    @PostMapping("/{requestId}/reasons/{reasonId}/attachments")
    public ResponseEntity<ChResponseBody<AttachmentDTO>> uploadAttachmentToRequest(
            @RequestParam("file") MultipartFile file, @PathVariable String requestId,
            @PathVariable String reasonId, HttpServletRequest servletRequest) {
        try {
            ServiceResult<AttachmentDTO> result = attachmentsService.addAttachment(file,
                servletRequest.getRequestURI(), requestId, reasonId);
            return responseEntityFactory.createResponse(result);
        } catch(ServiceException e) {
            return responseEntityFactory.createResponse(ServiceResult.notFound());
        }
    }

    @DeleteMapping("/{requestId}/reasons/{reasonId}/attachments/{attachmentId}")
    public ResponseEntity<ChResponseBody<Void>> deleteAttachmentFromRequest(@PathVariable String requestId,
          @PathVariable String reasonId, @PathVariable String attachmentId) {
      try {
          ServiceResult<Void> result = attachmentsService.removeAttachment(requestId, reasonId,
              attachmentId);
          return responseEntityFactory.createResponse(result);
      } catch(ServiceException e) {
          return responseEntityFactory.createResponse(ServiceResult.notFound());
      }
    }

    @GetMapping("/{requestId}/reasons/{reasonId}/attachments/{attachmentId}")
    public String downloadAttachmentFromRequest(@PathVariable String requestId, @PathVariable String attachmentId) {
      return "Getting attachment";
    }	
}
