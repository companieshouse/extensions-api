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

    @PostMapping("/{requestId}/reasons")
    public String addReasonToRequest(@RequestBody Reason reason, @PathVariable String requestId) {
      return "Reason added: " + reason.toString();
    }

    @DeleteMapping("/{requestId}/reasons/{reasonId}")
    public boolean deleteReasonFromRequest(@PathVariable String requestId, @PathVariable String reasonId) {
      return false;
    }

    @PutMapping("/{requestId}/reasons/{reasonId}")
    public String updateReasonOnRequest(@RequestBody Reason reason, @PathVariable String requestId, @PathVariable String reasonId) {
      return "Reason updated: " + reason.toString();
    }	
}