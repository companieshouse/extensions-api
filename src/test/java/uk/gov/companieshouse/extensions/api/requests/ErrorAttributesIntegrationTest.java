package uk.gov.companieshouse.extensions.api.requests;

import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.extensions.api.attachments.AttachmentsController;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;
import uk.gov.companieshouse.extensions.api.reasons.ReasonsController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("IntegrationTest")
@ExtendWith(SpringExtension.class)
@WebMvcTest()
class ErrorAttributesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AttachmentsController attachmentsController;

    @MockitoBean
    private ReasonsController reasonsController;

    @MockitoBean
    private RequestsController requestsController;

    @MockitoBean
    private RestTemplate restTemplate;

    @MockitoBean
    private ApiLogger apiLogger;

    /**
     * This tests that the error responses generated from Spring are being overridden to return
     * no content. The configuration to customise the error responses is in the ErrorAttributes bean
     * in ApplicationConfiguration class.
     * When a Spring error occurs, eg. no request body on a POST, Spring forwards to a Spring created "/error"
     * address that builds the json error response - hence why we need to hit "/error" to test the error
     * responses.
     *
     * @throws Exception
     */
    @Test
    void testError() throws Exception {
        this.mockMvc
            .perform(get("/error")
                .requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 400)
                .requestAttr(RequestDispatcher.ERROR_MESSAGE, "The request body is missing"))
            .andExpect(status().isBadRequest())
            .andExpect(content().string(""));
    }
}
