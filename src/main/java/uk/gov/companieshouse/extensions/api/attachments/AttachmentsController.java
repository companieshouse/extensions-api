package uk.gov.companieshouse.extensions.api.attachments;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import uk.gov.companieshouse.extensions.api.requests.CreatedBy;
import uk.gov.companieshouse.extensions.api.requests.ERICHeaderParser;
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.rest.response.ChResponseBody;
import uk.gov.companieshouse.service.rest.response.PluggableResponseEntityFactory;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/company/{companyNumber}/extensions/requests")
public class AttachmentsController {

    private PluggableResponseEntityFactory responseEntityFactory;
    private AttachmentsService attachmentsService;
    private ERICHeaderParser ericHeaderParser;

    @Autowired
    public AttachmentsController(PluggableResponseEntityFactory responseEntityFactory,
                                 AttachmentsService attachmentsService,
                                 ERICHeaderParser ericHeaderParser) {
        this.responseEntityFactory = responseEntityFactory;
        this.attachmentsService = attachmentsService;
        this.ericHeaderParser = ericHeaderParser;
    }

    @PostMapping("/{requestId}/reasons/{reasonId}/attachments")
    public ResponseEntity<ChResponseBody<AttachmentDTO>> uploadAttachmentToRequest(
            @RequestParam("file") MultipartFile file, @PathVariable String requestId,
            @PathVariable String reasonId, HttpServletRequest servletRequest) throws Exception {
        CreatedBy createdBy = new CreatedBy();
        createdBy.setId(ericHeaderParser.getUserId(servletRequest));
        createdBy.setEmail(ericHeaderParser.getEmail(servletRequest));
        createdBy.setForename(ericHeaderParser.getForename(servletRequest));
        createdBy.setSurname(ericHeaderParser.getSurname(servletRequest));

        ServiceResult<AttachmentDTO> result = attachmentsService.addAttachment(file,
            servletRequest.getRequestURI(), requestId, reasonId);
        return responseEntityFactory.createResponse(result);
    }

    @DeleteMapping("/{requestId}/reasons/{reasonId}/attachments/{attachmentId}")
    public boolean deleteAttachmentFromRequest(@PathVariable String requestId, @PathVariable String attachmentId) {
      return false;
    }

    @GetMapping("/{requestId}/reasons/{reasonId}/attachments/{attachmentId}")
    public String downloadAttachmentFromRequest(@PathVariable String requestId, @PathVariable String attachmentId) {
      return "Getting attachment";
    }	
}
