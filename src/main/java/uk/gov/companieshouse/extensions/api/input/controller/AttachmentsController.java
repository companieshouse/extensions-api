package uk.gov.companieshouse.extensions.api.input.controller;

import java.io.IOException;

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

    @PostMapping("/{requestId}/attachments")
    public String uploadAttachmentToRequest(@RequestParam("file") MultipartFile file, @PathVariable String requestId) {
      try {
        return "Attachment added " + new String(file.getBytes(), "UTF-8");
      } catch (IOException e) {
       // TODO Auto-generated catch block
       e.printStackTrace();
      }
      return requestId;
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
