package uk.gov.companieshouse.extensions.api.reasons;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import uk.gov.companieshouse.extensions.api.reasons.ReasonsController;

@RunWith(SpringRunner.class)
@WebMvcTest(value = ReasonsController.class)
public class ReasonsControllerTests {

    private static final String ROOT_URL = "/api/extensions/requests/a1/extensionReasons/";
    private static final String SPECIFIC_URL = "/api/extensions/requests/a1/extensionReasons/b2";

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testAddReasonToRequest() throws Exception {
         RequestBuilder requestBuilder = MockMvcRequestBuilders.post(
                 ROOT_URL)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(buildMockReason())
                  .accept(MediaType.APPLICATION_JSON);

          MvcResult result = mockMvc.perform(requestBuilder).andReturn();
          assertEquals("ExtensionReason added: ExtensionReason illness Additional text: string  Date start: 2019-02-15  Date end: 2019-02-15", result.getResponse().getContentAsString());
    }

    @Test
    public void testDeleteReasonFromRequest() throws Exception {
         RequestBuilder requestBuilder = MockMvcRequestBuilders.delete(
                 SPECIFIC_URL)
                 .accept(MediaType.APPLICATION_JSON);

         MvcResult result = mockMvc.perform(requestBuilder).andReturn();
         assertEquals("false", result.getResponse().getContentAsString());
    }
    
    @Test
    public void testUpdateReasonOnRequest() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.put(
                SPECIFIC_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildMockReason())
                .accept(MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        assertEquals("ExtensionReason updated: ExtensionReason illness Additional text: string  Date start: 2019-02-15  Date end: 2019-02-15", result.getResponse().getContentAsString());
    }

    private String buildMockReason() {
        return "{\n" + 
                "  \"reason\": \"illness\",\n" + 
                "  \"additional_text\": \"string\",\n" + 
                "  \"date_start\": \"2019-02-15\",\n" + 
                "  \"date_end\": \"2019-02-15\"\n" + 
                "}";
    }
}
