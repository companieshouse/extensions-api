package uk.gov.companieshouse.extensions.api.requests;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.extensions.api.Utils.Utils;
import uk.gov.companieshouse.extensions.api.authorization.CompanyAuthorizationInterceptor;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.companieshouse.extensions.api.Utils.Utils.COMPANY_NUMBER;

@Tag("IntegrationTest")
@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = {"EXTENSIONS_API_MONGODB_URL=mongodb://mongo-db1-toro1.development.aws.internal:27017", "server.port=8093",
    "api.endpoint.extensions=/company/{companyNumber}/extensions/requests",
    "spring.data.mongodb.uri=mongodb://mongo-db1-toro1.development.aws.internal:27017/extension_requests",
    "FILE_TRANSFER_API_URL=http://localhost:8081/",
    "FILE_TRANSFER_API_KEY=12345",
    "MONGO_CONNECTION_POOL_MIN_SIZE=0",
    "MONGO_CONNECTION_MAX_IDLE_TIME=0",
    "MONGO_CONNECTION_MAX_LIFE_TIME=0",
    "spring.servlet.multipart.max-file-size=100",
    "spring.servlet.multipart.max-request-size=200"})
@SpringBootTest(classes = RequestsController.class)
public class RequestControllerIntegrationTest {

    private static final String ROOT_URL = "/company/00006400/extensions/requests";
    private static final String REQUEST_BY_ID_URL = "/company/00006400/extensions/requests/a1";

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

    @MockBean
    private CompanyAuthorizationInterceptor companyInterceptor;

    @InjectMocks
    RequestsController requestsController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(requestsController).addPlaceholderValue("api.endpoint.extensions", "/company/{companyNumber}/extensions/requests").build();
        autowireFields(requestsController, requestsService,
            ericHeaderParser, extensionRequestMapper,
            apiLogger);
        when(companyInterceptor.preHandle(any(HttpServletRequest.class), any(HttpServletResponse.class),
            any(Object.class)))
            .thenReturn(true);
    }

    public void autowireFields(RequestsController requestsController, RequestsService requestsService,
                               ERICHeaderParser ericHeaderParser, ExtensionRequestMapper extensionRequestMapper,
                               ApiLogger logger) {
        autowireField(requestsController, "requestsService", requestsService);
        autowireField(requestsController, "ericHeaderParser", ericHeaderParser);
        autowireField(requestsController, "extensionRequestMapper", extensionRequestMapper);
        autowireField(requestsController, "logger", logger);
    }

    private void autowireField(Object targetObject, String fieldName, Object fieldValue) {
        Field field = ReflectionUtils.findField(targetObject.getClass(), fieldName);
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, targetObject, fieldValue);
    }

    @Test
    public void testCreateExtensionRequestResource() throws Exception {
        String request = buildMockRequest();

        when(extensionRequestMapper.entityToDTO(
            any(ExtensionRequestFullEntity.class)))
            .thenReturn(Utils.dummyRequestDTO());
        when(requestsService.insertExtensionsRequest(
            any(ExtensionCreateRequest.class),
            any(CreatedBy.class),
            any(String.class),
            any(String.class)))
            .thenReturn(Utils.dummyRequestEntity());

        RequestBuilder requestBuilder = MockMvcRequestBuilders.post(
                ROOT_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .content(request)
            .accept(MediaType.APPLICATION_JSON);

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        Assertions.assertEquals(201, result.getResponse().getStatus());
    }

    @Test
    public void testGetExtensionRequestsList() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .get(ROOT_URL)
            .accept(MediaType.APPLICATION_JSON);

        ExtensionRequestFullEntity extensionRequestFullEntity = Utils.dummyRequestEntity();
        ExtensionRequestFullDTO extensionRequestFullDTO = Utils.dummyRequestDTO();
        List<ExtensionRequestFullEntity> extensionRequestFullEntityList = new ArrayList<>();
        extensionRequestFullEntityList.add(extensionRequestFullEntity);

        when(requestsService.getExtensionsRequestListByCompanyNumber(COMPANY_NUMBER)).thenReturn(extensionRequestFullEntityList);
        when(extensionRequestMapper.entityToDTO(extensionRequestFullEntity)).thenReturn
            (extensionRequestFullDTO);

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        Assertions.assertEquals(200, result.getResponse().getStatus());

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
        Assertions.assertEquals(200, result.getResponse().getStatus());
    }

    private String buildMockRequest() {
        return "{\n" +
            "  \"user\": \"Micky Mock\",\n" +
            "  \"accounting_period_start_date\": \"2019-02-15\",\n" +
            "  \"accounting_period_end_date\": \"2019-02-15\",\n" +
            "  \"extensionReasons\": [\n" +
            "    {\n" +
            "      \"reason\": \"string\",\n" +
            "      \"reason_information\": \"string\",\n" +
            "      \"date_start\": \"2019-02-15\",\n" +
            "      \"date_end\": \"2019-02-15\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";
    }
}
