package uk.gov.companieshouse.extensions.api.reasons;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.endpoint.extensions}")
public class ReasonsController {

    @PostMapping("/{requestId}/extensionReasons")
    public String addReasonToRequest(@RequestBody ExtensionReason extensionReason, @PathVariable String requestId) {
      return "ExtensionReason added: " + extensionReason.toString();
    }

    @DeleteMapping("/{requestId}/extensionReasons/{reasonId}")
    public boolean deleteReasonFromRequest(@PathVariable String requestId, @PathVariable String reasonId) {
      return false;
    }

    @PutMapping("/{requestId}/extensionReasons/{reasonId}")
    public String updateReasonOnRequest(@RequestBody ExtensionReason extensionReason, @PathVariable String requestId, @PathVariable String reasonId) {
      return "ExtensionReason updated: " + extensionReason.toString();
    }	
}
