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
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.extensions.api.Utils.Utils;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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

    @MockBean
    private Supplier<LocalDateTime> localDateTimeSupplier;

    @MockBean
    private ERICHeaderParser ericHeaderParser;

    @MockBean
    private ExtensionRequestMapper extensionRequestMapper;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private ApiLogger apiLogger;

    @Test
    public void testCreateExtensionRequestResource() throws Exception {
        String request = buildMockRequest();

        when(extensionRequestMapper.entityToDTO(
            any(ExtensionRequestFullEntity.class)))
            .thenReturn(Utils.dummyRequestDTO());
        when(requestsService.insertExtensionsRequest(
            any(ExtensionCreateRequest.class),
            any(CreatedBy.class),
            any(String.class)))
            .thenReturn(Utils.dummyRequestEntity());

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
        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .get(REQUEST_BY_ID_URL)
            .accept(MediaType.APPLICATION_JSON);

        ExtensionRequestFullEntity extensionRequestFullEntity = Utils.dummyRequestEntity();
        ExtensionRequestFullDTO extensionRequestFullDTO = Utils.dummyRequestDTO();

        when(requestsService.getExtensionsRequestById("a1")).thenReturn(Optional.of(extensionRequestFullEntity));
        when(extensionRequestMapper.entityToDTO(extensionRequestFullEntity)).thenReturn(extensionRequestFullDTO);

        mockMvc.perform(requestBuilder)
            .andExpect(status().isOk());
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
