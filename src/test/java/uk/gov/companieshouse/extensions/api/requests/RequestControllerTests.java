package uk.gov.companieshouse.extensions.api.requests;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.time.Month;

import com.mongodb.MongoClientOptions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@RunWith(SpringRunner.class)
@WebMvcTest(value = RequestsController.class)
public class RequestControllerTests {

    private static final String ROOT_URL = "/api/extensions/requests/";
    private static final String REQUEST_BY_ID_URL = "/api/extensions/requests/a1";

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
      assertEquals("Request received: User Micky Mock Acc period start: 2019-02-15  Acc period end: 2019-02-15", result.getResponse().getContentAsString());
    }

   @Test
    public void testGetExtensionRequestsList() throws Exception {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.get(
             ROOT_URL).accept(
                 MediaType.APPLICATION_JSON);

      MvcResult result = mockMvc.perform(requestBuilder).andReturn();
      assertEquals("", result.getResponse().getContentAsString());
    }
    
    @Test
    public void testGetSingleExtentionRequest() throws Exception {
      ExtensionRequest mockExtensionRequest = buildMockExtensionsRequest();
      Mockito.when(
        requestsService.getExtensionsRequestById(Mockito.anyString()))
           .thenReturn(mockExtensionRequest);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.get(
             REQUEST_BY_ID_URL).accept(
                  MediaType.APPLICATION_JSON);
      
      MvcResult result = mockMvc.perform(requestBuilder).andReturn();
      String expected = 
          "{\"user\":\"Micky Mock\",\"extensionReasons"
          + "\":null,\"accounting_period_start_date\":\"2017-07-01\","
          + "\"accounting_period_end_date\":\"2018-06-30\"}";

      JSONAssert.assertEquals(expected, result.getResponse()
           .getContentAsString(), false);
    }

    @Test
    public void testDeleteExtensionRequest() throws Exception {
      RequestBuilder requestBuilder = MockMvcRequestBuilders.delete(
             REQUEST_BY_ID_URL).accept(
                 MediaType.APPLICATION_JSON);

      MvcResult result = mockMvc.perform(requestBuilder).andReturn();
      assertEquals("false", result.getResponse().getContentAsString());
    }

    private ExtensionRequest buildMockExtensionsRequest() {
         return ExtensionRequest.builder()
             .user("Micky Mock")
             .accountingPeriodStartDate(LocalDate.of(2017, Month.JULY, 1))
             .accountingPeriodEndDate(LocalDate.of(2018, Month.JUNE, 30))
             .build();
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
