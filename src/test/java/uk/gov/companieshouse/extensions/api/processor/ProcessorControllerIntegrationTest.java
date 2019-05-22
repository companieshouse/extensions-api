package uk.gov.companieshouse.extensions.api.processor;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestTemplate;

import uk.gov.companieshouse.extensions.api.groups.Integration;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;

@Category(Integration.class)
@RunWith(SpringRunner.class)
@WebMvcTest(value = ProcessorController.class)
public class ProcessorControllerIntegrationTest {
 
    public static String URL = "/company/00006400/extensions/requests/a1/status";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApiLogger apiLogger;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    public void statusEndpointCanBeReached() throws Exception {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.post(
             URL)
              .contentType(MediaType.APPLICATION_JSON)
              .accept(MediaType.APPLICATION_JSON);

      MvcResult result = mockMvc.perform(requestBuilder).andReturn();
      assertEquals(200, result.getResponse().getStatus());
    }

}
