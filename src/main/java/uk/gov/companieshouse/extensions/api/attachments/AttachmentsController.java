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

import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.rest.response.ChResponseBody;
import uk.gov.companieshouse.service.rest.response.PluggableResponseEntityFactory;

@RestController
@RequestMapping("${api.endpoint.extensions}")
public class AttachmentsController {

    private PluggableResponseEntityFactory responseEntityFactory;

    @Autowired
    public AttachmentsController(PluggableResponseEntityFactory responseEntityFactory) {
        this.responseEntityFactory = responseEntityFactory;
    }

    @PostMapping("/{requestId}/attachments")
    public ResponseEntity<ChResponseBody<AttachmentsMetadata>> uploadAttachmentToRequest(
            @RequestParam("file") MultipartFile file, @PathVariable String requestId) {
        return responseEntityFactory.createResponse(
                ServiceResult.accepted(new AttachmentsMetadata("/dummy.url", "scanned")));
    }

    @DeleteMapping("/{requestId}/attachments/{attachmentId}")
    public boolean deleteAttachmentFromRequest(@PathVariable String requestId, @PathVariable String attachmentId) {
      return false;
    }

    @GetMapping("/{requestId}/attachments/{attachmentId}")
    public String downloadAttachmentFromRequest(@PathVariable String requestId, @PathVariable String attachmentId) {
      return "Getting attachment";
    }	
}
