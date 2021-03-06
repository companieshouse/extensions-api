package uk.gov.companieshouse.extensions.api.reasons;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.dummyReasonEntity;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.dummyRequestEntity;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;
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

import uk.gov.companieshouse.extensions.api.authorization.CompanyAuthorizationInterceptor;
import uk.gov.companieshouse.extensions.api.groups.Integration;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestFullEntity;
import uk.gov.companieshouse.extensions.api.requests.ExtensionsLinkKeys;
import uk.gov.companieshouse.extensions.api.response.ListResponse;
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.links.Links;

@Category(Integration.class)
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
    private ExtensionReasonMapper mapper;

    @MockBean
    private HttpServletRequest mockHttpServletRequest;

    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private ApiLogger apiLogger;

    @MockBean
    private CompanyAuthorizationInterceptor companyInterceptor;

    @Before
    public void setup() {
        when(companyInterceptor.preHandle(any(HttpServletRequest.class), 
            any(HttpServletResponse.class), any(Object.class)))
              .thenReturn(true);
    }

    @Test
    public void canReachPostReasonEndpoint() throws Exception {

         ExtensionReasonDTO dto = new ExtensionReasonDTO();
         dto.setId("123");

         Links links = new Links();
         links.setLink(ExtensionsLinkKeys.SELF, "url");
         dto.setLinks(links);
         when(reasonsService.addExtensionsReasonToRequest(any(ExtensionCreateReason.class), any(String.class), any(String.class)))
             .thenReturn(ServiceResult.created(dto));
         RequestBuilder requestBuilder = MockMvcRequestBuilders.post(
                 ROOT_URL)
                  .contentType(MediaType.APPLICATION_JSON)
                  .content(buildMockReason())
                  .accept(MediaType.APPLICATION_JSON);

          MvcResult result = mockMvc.perform(requestBuilder).andReturn();
          assertEquals(201, result.getResponse().getStatus());
          assertEquals(mockReponse(), result.getResponse().getContentAsString());
    }

    @Test
    public void canReachDeleteReasonEndpoint() throws Exception {

        ExtensionRequestFullEntity dummyRequestEntity = dummyRequestEntity();
        dummyRequestEntity.addReason(dummyReasonEntity());

        when(reasonsService.removeExtensionsReasonFromRequest(any(String.class), any
            (String.class))).thenReturn(dummyRequestEntity);
         RequestBuilder requestBuilder = MockMvcRequestBuilders.delete(
                 SPECIFIC_URL)
                 .accept(MediaType.APPLICATION_JSON);

         MvcResult result = mockMvc.perform(requestBuilder).andReturn();
         assertEquals(204, result.getResponse().getStatus());
    }
    
    @Test
    public void canReachUpdateReasonEndpoint() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.patch(
                SPECIFIC_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildMockReason())
                .accept(MediaType.APPLICATION_JSON);

        Links links = new Links();
        links.setLink(ExtensionsLinkKeys.SELF, "url");
        ExtensionReasonDTO dto = new ExtensionReasonDTO();
        dto.setLinks(links);
        when(reasonsService.patchReason(any(ExtensionCreateReason.class), any(String.class), any(String.class)))
            .thenReturn(dto);
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        assertEquals(200, result.getResponse().getStatus());
    }

    @Test
    public void canReachGetReasonsEndPoint() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders.get(ROOT_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON);

        ServiceResult<ListResponse<ExtensionReasonDTO>> expectedResult =
            ServiceResult.found(ListResponse.<ExtensionReasonDTO>builder()
                .withItems(Arrays.asList(new ExtensionReasonDTO()))
                .build());
        when(reasonsService.getReasons(anyString()))
            .thenReturn(expectedResult);
        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        assertEquals(200, result.getResponse().getStatus());
        assertEquals(mockGetResponse(), result.getResponse().getContentAsString());
    }

    private String buildMockReason() {
        return "{\n" +
                "  \"reason\": \"illness\",\n" +
                "  \"reason_information\": \"string\",\n" +
                "  \"date_start\": \"2019-02-15\",\n" +
                "  \"date_end\": \"2019-02-15\"\n" +
                "}";
    }

    private String mockReponse() {
        return "{\"etag\":null," +
            "\"id\":\"123\"," +
            "\"reason\":null," +
            "\"links\":{" +
                "\"self\":\"url\"" +
            "}," +
            "\"start_on\":null," +
            "\"end_on\":null," +
            "\"affected_person\":null," +
            "\"reason_information\":null," +
            "\"continued_illness\":null," +
            "\"reason_status\":null" +
        "}";
    }

    private String mockGetResponse() {
        return "{" +
            "\"etag\":null," +
            "\"items\":[" +
                "{\"etag\":null," +
                "\"id\":null," +
                "\"reason\":null," +
                "\"start_on\":null," +
                "\"end_on\":null," +
                "\"affected_person\":null," +
                "\"reason_information\":null," +
                "\"continued_illness\":null," +
                "\"reason_status\":null" +
            "}]," +
            "\"items_per_page\":0," +
            "\"start_index\":0," +
            "\"total_results\":1" +
            "}";
    }
}
