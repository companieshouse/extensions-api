package uk.gov.companieshouse.extensions.api.requests;

import org.junit.Test;
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

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@WebMvcTest(value = RequestsController.class)
public class RequestControllerIntegrationTest {

    private static final String ROOT_URL = "/company/00006400/extensions/requests/";
    private static final String REQUEST_BY_ID_URL = "/company/00006400/extensions/requests/a1";

    @Autowired
    private MockMvc mockMvc;
 
    @MockBean
    private RequestsService requestsService;

    @MockBean
    private ExtensionRequestsRepository extensionRequestsRepository;

    @Test
    public void testCreateExtensionRequestResource() throws Exception {
        String request = buildMockRequest();
        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(
             ROOT_URL)
              .contentType(MediaType.APPLICATION_JSON)
              .content(request)
              .accept(MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        assertEquals(201, result.getResponse().getStatus());
    }

    @Test
    public void testGetExtensionRequestsList() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .get(ROOT_URL)
            .accept(MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        assertEquals(200, result.getResponse().getStatus());

    }
    
    @Test
    public void testGetSingleExtensionRequest() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(
             REQUEST_BY_ID_URL).accept(
                  MediaType.APPLICATION_JSON);
      
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();

        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void testDeleteExtensionRequest() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .delete(REQUEST_BY_ID_URL)
            .accept(MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    private String buildMockRequest() {
        return "{\n" +
               "  \"user\": \"Micky Mock\",\n" +
               "  \"accounting_period_start_date\": \"2019-02-15\",\n" +
               "  \"accounting_period_end_date\": \"2019-02-15\",\n" +
               "  \"extensionReasons\": [\n" +
               "    {\n" +
               "      \"reason\": \"string\",\n" +
               "      \"additional_text\": \"string\",\n" +
               "      \"date_start\": \"2019-02-15\",\n" +
               "      \"date_end\": \"2019-02-15\"\n" +
               "    }\n" +
               "  ]\n" +
               "}";
      }
}