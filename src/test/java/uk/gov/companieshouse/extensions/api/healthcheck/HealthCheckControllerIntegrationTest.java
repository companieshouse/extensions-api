package uk.gov.companieshouse.extensions.api.healthcheck;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.companieshouse.extensions.api.authorization.CompanyAuthorizationInterceptor;
import uk.gov.companieshouse.extensions.api.config.HealthCheckController;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Tag("IntegrationTest")
@ExtendWith(SpringExtension.class)
class HealthCheckControllerIntegrationTest {

    private static final String ROOT_URL = "/extensions-api/healthcheck";


    private MockMvc mockMvc;
    @MockitoBean
    private CompanyAuthorizationInterceptor companyInterceptor;

    @BeforeEach
    void setup() {

        mockMvc = MockMvcBuilders.standaloneSetup(new HealthCheckController()).build();

        when(companyInterceptor.preHandle(any(),
            any(), any()))
            .thenReturn(true);
    }

    @Test
    void canReachHealthCheckReasonEndpoint() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(
            ROOT_URL);
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }
}
