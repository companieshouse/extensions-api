package uk.gov.companieshouse.extensions.api.config;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/extensions-api/healthcheck")
    public ResponseEntity<String> healthcheck() {
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
