package uk.gov.companieshouse.extensions.api.input.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.gov.companieshouse.extensions.api.input.dto.Reason;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@RestController
@RequestMapping("/api/extensions/requests")
public class ReasonsController {

    private static final Logger LOGGER = LoggerFactory.getLogger("controller.input.api.extensions.ch.gov.uk");

    @PostMapping("/{requestId}/reasons")
    public String addReasonToRequest(@RequestBody Reason reason, @PathVariable String requestId) {
      String response = "Reason added: " + reason.toString();
      LOGGER.info(response);
      return response;
    }

    @DeleteMapping("/{requestId}/reasons/{reasonId}")
    public boolean deleteReasonFromRequest(@PathVariable String requestId, @PathVariable String reasonId) {
      boolean result = false;
      Map<String, Object> logData = new HashMap<String, Object>();
      logData.put("Deleted", result);
      LOGGER.infoContext(requestId, "", logData);
      return result;
    }

    @PutMapping("/{requestId}/reasons/{reasonId}")
    public String updateReasonOnRequest(@RequestBody Reason reason, @PathVariable String requestId, @PathVariable String reasonId) {
      String response = "Reason updated: " + reason.toString();
      LOGGER.info(response);
      return response;
    }
}
