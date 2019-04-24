package uk.gov.companieshouse.extensions.api.reasons;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.dummyCreateReason;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.dummyReasonEntity;

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

import uk.gov.companieshouse.extensions.api.reasons.ReasonsController;

@RunWith(SpringRunner.class)
@WebMvcTest(value = ReasonsController.class)
public class ReasonsControllerIntegrationTest {

    private static final String ROOT_URL = "/company/00006400/extensions/requests/a1/reasons/";
    private static final String SPECIFIC_URL = "/company/00006400/extensions/requests/a1/reasons" +
        "/b2";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReasonsService reasonsService;

    @MockBean
    private ExtensionReasonMapper extensionReasonMapper;

    @Test
    public void canReachPostReasonEndpoint() throws Exception {
         ExtensionReasonEntity dummyReasonEntity = dummyReasonEntity();
         when(reasonsService.insertExtensionsReason(any(ExtensionCreateReason.class))).thenReturn(dummyReasonEntity);
         RequestBuilder requestBuilder = MockMvcRequestBuilders.post(
                 ROOT_URL)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(buildMockReason())
                  .accept(MediaType.APPLICATION_JSON);

          MvcResult result = mockMvc.perform(requestBuilder).andReturn();
          assertEquals(201, result.getResponse().getStatus());
    }

    @Test
    public void canReachDeleteReasonEndpoint() throws Exception {
         RequestBuilder requestBuilder = MockMvcRequestBuilders.delete(
                 SPECIFIC_URL)
                 .accept(MediaType.APPLICATION_JSON);

         MvcResult result = mockMvc.perform(requestBuilder).andReturn();
         assertEquals(200, result.getResponse().getStatus());
    }
    
    @Test
    public void canReachUpdateReasonEndpoint() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.put(
                SPECIFIC_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildMockReason())
                .accept(MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    String buildMockReason() {
        return "{\n" +
                "  \"reason\": \"illness\",\n" +
                "  \"additional_text\": \"string\",\n" +
                "  \"date_start\": \"2019-02-15\",\n" +
                "  \"date_end\": \"2019-02-15\"\n" +
                "}";
    }
}
