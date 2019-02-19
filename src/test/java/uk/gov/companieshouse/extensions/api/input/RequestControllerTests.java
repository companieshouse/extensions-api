package uk.gov.companieshouse.extensions.api.input;

import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.time.Month;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import uk.gov.companieshouse.extensions.api.input.controller.RequestsController;
import uk.gov.companieshouse.extensions.api.input.dto.ExtensionsRequest;
import uk.gov.companieshouse.extensions.api.input.service.RequestsService;

@RunWith(SpringRunner.class)
@WebMvcTest(value = RequestsController.class)
public class RequestControllerTests {

    private static final String ROOT_URL = "/api/extensions/requests/";
    private static final String REQUEST_BY_ID_URL = "/api/extensions/requests/a1";

    @Autowired
    private MockMvc mockMvc;
 
    @MockBean
    private RequestsService requestsService;

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
    
    private String buildMockRequest() {
      return "{\n" + 
            "  \"user\": \"Micky Mock\",\n" + 
            "  \"accounting_period_start_date\": \"2019-02-15\",\n" + 
            "  \"accounting_period_end_date\": \"2019-02-15\",\n" + 
            "  \"reasons\": [\n" + 
            "    {\n" + 
            "      \"reason\": \"string\",\n" + 
            "      \"additional_text\": \"string\",\n" + 
            "      \"date_start\": \"2019-02-15\",\n" + 
            "      \"date_end\": \"2019-02-15\"\n" + 
            "    }\n" + 
            "  ]\n" + 
            "}"; 
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
      ExtensionsRequest mockExtensionsRequest = buildMockExtensionsRequest();
      Mockito.when(
        requestsService.getExtensionsRequestById(Mockito.anyString()))
           .thenReturn(mockExtensionsRequest);

      RequestBuilder requestBuilder = MockMvcRequestBuilders.get(
             REQUEST_BY_ID_URL).accept(
                  MediaType.APPLICATION_JSON);
      
      MvcResult result = mockMvc.perform(requestBuilder).andReturn();
      String expected = 
          "{\"user\":\"Micky Mock\",\"reasons"
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

    private ExtensionsRequest buildMockExtensionsRequest() {
         ExtensionsRequest extensionsRequest =  new ExtensionsRequest();
         extensionsRequest.setUser("Micky Mock");     
         extensionsRequest.setAccountingPeriodStartDate(LocalDate.of(2017, Month.JULY, 1));     
         extensionsRequest.setAccountingPeriodEndDate(LocalDate.of(2018, Month.JUNE, 30));
         return extensionsRequest;		
    }
}
