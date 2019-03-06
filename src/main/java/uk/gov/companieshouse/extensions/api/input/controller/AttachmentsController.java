package uk.gov.companieshouse.extensions.api.input.controller;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/extensions/requests")
public class AttachmentsController {

    private static final Logger LOGGER = LoggerFactory.getLogger("controller.input.api.extensions.ch.gov.uk");

    @PostMapping("/{requestId}/attachments")
    public String uploadAttachmentToRequest(@RequestParam("file") MultipartFile file, @PathVariable String requestId) {
      try {
        String response = "Attachment added " + new String(file.getBytes(), "UTF-8");
        LOGGER.info(response);
        return response;
      } catch (IOException e) {
        LOGGER.error("ERROR: " + e.getMessage());
        e.printStackTrace();
      }
      return requestId;
    }

    @DeleteMapping("/{requestId}/attachments/{attachmentId}")
    public boolean deleteAttachmentFromRequest(@PathVariable String requestId, @PathVariable String attachmentId) {
      boolean result = false;
      Map<String, Object> logData = new HashMap<String, Object>();
      logData.put("Deleted ", result);
      LOGGER.infoContext(requestId, "", logData);
      return result;
    }

    @GetMapping("/{requestId}/attachments/{attachmentId}")
    public String downloadAttachmentFromRequest(@PathVariable String requestId, @PathVariable String attachmentId) {
      String response = "Getting attachment";
      LOGGER.info(response);
      return response;
    }
}
