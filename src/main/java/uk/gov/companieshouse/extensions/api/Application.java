package uk.gov.companieshouse.extensions.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@SpringBootApplication
public class Application {

  public static final String APP_NAMESPACE = "extensions-api";

  public static void main(String[] args) {
    SpringApplication.run(Application.class);
  }

    @Autowired
    private ApiLogger logger;

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        logger.info(String.format("Set Spring Boot Time Zone to %s", TimeZone.getDefault().getID()));
    }
}
