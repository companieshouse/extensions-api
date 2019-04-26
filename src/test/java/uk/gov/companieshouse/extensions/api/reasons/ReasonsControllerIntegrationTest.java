package uk.gov.companieshouse.extensions.api.reasons;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestFullDTO;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestFullEntity;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestMapper;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestsRepository;

import javax.servlet.http.HttpServletRequest;

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
    private ExtensionRequestMapper extensionRequestMapper;

    @MockBean
    private ExtensionRequestsRepository extensionRequestsRepository;

    @MockBean
    private HttpServletRequest mockHttpServletRequest;

    @Test
    public void canReachPostReasonEndpoint() throws Exception {

         ExtensionRequestFullEntity dummyRequestEntity = dummyRequestEntity();
         dummyRequestEntity.addReason(dummyReasonEntity());

         ExtensionRequestFullDTO entityRequestDTO = dummyRequestDTO();

         when(reasonsService.addExtensionsReasonToRequest(any(ExtensionCreateReason.class), any(String.class), any(String.class))).thenReturn(dummyRequestEntity);
         when(extensionRequestMapper.entityToDTO(dummyRequestEntity)).thenReturn(entityRequestDTO);
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

        ExtensionRequestFullEntity dummyRequestEntity = dummyRequestEntity();
        dummyRequestEntity.addReason(dummyReasonEntity());

        ExtensionRequestFullDTO entityRequestDTO = dummyRequestDTO();

        when(reasonsService.removeExtensionsReasonFromRequest(any(String.class), any
            (String.class))).thenReturn(dummyRequestEntity);
        when(extensionRequestMapper.entityToDTO(dummyRequestEntity)).thenReturn(entityRequestDTO);
         RequestBuilder requestBuilder = MockMvcRequestBuilders.delete(
                 SPECIFIC_URL)
                 .accept(MediaType.APPLICATION_JSON);

         MvcResult result = mockMvc.perform(requestBuilder).andReturn();
         assertEquals(204, result.getResponse().getStatus());
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
