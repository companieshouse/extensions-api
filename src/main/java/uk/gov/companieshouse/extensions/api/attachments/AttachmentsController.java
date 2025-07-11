package uk.gov.companieshouse.extensions.api.attachments;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;
import uk.gov.companieshouse.extensions.api.logger.LogMethodCall;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.rest.response.ChResponseBody;
import uk.gov.companieshouse.service.rest.response.PluggableResponseEntityFactory;

@RestController
@RequestMapping("/company/{companyNumber}/extensions/requests")
public class AttachmentsController {

    private final PluggableResponseEntityFactory responseEntityFactory;
    private final AttachmentsService attachmentsService;
    private final ApiLogger logger;

    @Autowired
    public AttachmentsController(PluggableResponseEntityFactory responseEntityFactory,
                                 AttachmentsService attachmentsService, ApiLogger logger) {
        this.responseEntityFactory = responseEntityFactory;
        this.attachmentsService = attachmentsService;
        this.logger = logger;
    }

    @LogMethodCall
    @PostMapping("/{requestId}/reasons/{reasonId}/attachments")
    public ResponseEntity<ChResponseBody<AttachmentDTO>> uploadAttachmentToRequest(
        @RequestParam("file") MultipartFile file, @PathVariable String requestId, @PathVariable String reasonId,
        HttpServletRequest servletRequest) {
        try {
            ServiceResult<AttachmentDTO> result = attachmentsService.addAttachment(file,
                servletRequest.getRequestURI(), requestId, reasonId);
            return responseEntityFactory.createResponse(result);

        } catch(ServiceException e) {
            logger.error(e);
            return responseEntityFactory.createResponse(ServiceResult.notFound());

        } catch(HttpClientErrorException | HttpServerErrorException e) {
            logger.error(String.format("The file-transfer-api has returned an error for file: %s",
                file.getOriginalFilename()), e);
            return ResponseEntity.status(e.getStatusCode()).build();
        }
    }

    @LogMethodCall
    @DeleteMapping("/{requestId}/reasons/{reasonId}/attachments/{attachmentId}")
    public ResponseEntity<ChResponseBody<Void>> deleteAttachmentFromRequest(
        @PathVariable String requestId, @PathVariable String reasonId, @PathVariable String attachmentId) {
      try {
          ServiceResult<Void> result = attachmentsService.removeAttachment(requestId, reasonId, attachmentId);
          return responseEntityFactory.createResponse(result);

      } catch(ServiceException e) {
          logger.info(e.getMessage());
          return responseEntityFactory.createResponse(ServiceResult.notFound());
      }
    }

    @LogMethodCall
    @GetMapping("/{requestId}/reasons/{reasonId}/attachments/{attachmentId}/download")
    public void downloadAttachmentFromRequest(@PathVariable String attachmentId, HttpServletResponse response) {
        attachmentsService.downloadAttachment(attachmentId, response);
    }
}
