package uk.gov.companieshouse.extensions.api.authorization;

import static org.mockito.Mockito.lenient;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import uk.gov.companieshouse.extensions.api.logger.ApiLogger;
import uk.gov.companieshouse.extensions.api.requests.CreatedBy;
import uk.gov.companieshouse.extensions.api.requests.ExtensionRequestFullEntity;
import uk.gov.companieshouse.extensions.api.requests.RequestsService;

@Tag("UnitTest")
@ExtendWith(MockitoExtension.class)
class CompanyAuthorizationInterceptorTest {

    private static final String BASE = "/company/00006400/extensions/requests";
    private static final String DOWNLOAD = BASE + "/1/reasons/2/attachments/3/download";
    private static final String OWNER = "owner-id";
    private static final String VIEW = "/admin/extensions-view";
    private static final String VIEW_AND_DOWNLOAD = "/admin/extensions-view /admin/extensions-download";

    @Mock
    private ApiLogger logger;

    @Mock
    private RequestsService requestsService;

    private CompanyAuthorizationInterceptor interceptor;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        interceptor = new CompanyAuthorizationInterceptor(logger, requestsService);
        response = new MockHttpServletResponse();

        ExtensionRequestFullEntity entity = new ExtensionRequestFullEntity();
        CreatedBy createdBy = new CreatedBy();
        createdBy.setId(OWNER);
        entity.setCreatedBy(createdBy);
        lenient().when(requestsService.getExtensionsRequestById("00006400", "1")).thenReturn(Optional.of(entity));
    }

    private boolean handle(String method, String uri, String identity, String type, String roles) {
        MockHttpServletRequest request = new MockHttpServletRequest(method, uri);
        request.setRequestURI(uri);
        if (identity != null) {
            request.addHeader("ERIC-Identity", identity);
        }
        if (type != null) {
            request.addHeader("ERIC-Identity-Type", type);
        }
        if (roles != null) {
            request.addHeader("ERIC-Authorised-Roles", roles);
        }
        return interceptor.preHandle(request, response, null);
    }

    @Test
    void missingIdentityIsUnauthorised() {
        Assertions.assertFalse(handle("GET", BASE + "/1", null, null, null));
        Assertions.assertEquals(401, response.getStatus());
    }

    @Test
    void adminCanOnlyDownload() {
        Assertions.assertTrue(handle("GET", DOWNLOAD, "admin", "oauth2", VIEW_AND_DOWNLOAD));
        Assertions.assertFalse(handle("GET", DOWNLOAD, "admin", "oauth2", VIEW));
        Assertions.assertFalse(handle("DELETE", DOWNLOAD, "admin", "oauth2", VIEW_AND_DOWNLOAD));
        Assertions.assertFalse(handle("GET", BASE, "admin", "oauth2", VIEW_AND_DOWNLOAD));
        Assertions.assertFalse(handle("GET", BASE + "/1", OWNER, "oauth2", VIEW_AND_DOWNLOAD));
        Assertions.assertFalse(handle("PATCH", BASE + "/1", "admin", "oauth2", VIEW_AND_DOWNLOAD));
        Assertions.assertFalse(handle("DELETE", BASE + "/1/reasons/2/attachments/3", "admin", "oauth2", VIEW));
        Assertions.assertEquals(403, response.getStatus());
    }

    @Test
    void usersAndKeysCannotDownload() {
        Assertions.assertFalse(handle("GET", DOWNLOAD, OWNER, "oauth2", null));
        Assertions.assertFalse(handle("GET", DOWNLOAD, "k", "key", null));
    }

    @Test
    void userCanCreateAndListOwnRequests() {
        Assertions.assertTrue(handle("POST", BASE, OWNER, "oauth2", null));
        Assertions.assertTrue(handle("GET", BASE, OWNER, "oauth2", null));
        Assertions.assertFalse(handle("POST", BASE, "k", "key", null));
    }

    @Test
    void listIsFilteredToOwnerForUsersOnly() {
        MockHttpServletRequest user = new MockHttpServletRequest("GET", BASE);
        user.setRequestURI(BASE);
        user.addHeader("ERIC-Identity", OWNER);
        user.addHeader("ERIC-Identity-Type", "oauth2");
        Assertions.assertTrue(interceptor.preHandle(user, response, null));
        Assertions.assertEquals(OWNER, user.getAttribute(CompanyAuthorizationInterceptor.OWNER_FILTER_ATTRIBUTE));

        MockHttpServletRequest key = new MockHttpServletRequest("GET", BASE);
        key.setRequestURI(BASE);
        key.addHeader("ERIC-Identity", "k");
        key.addHeader("ERIC-Identity-Type", "key");
        Assertions.assertTrue(interceptor.preHandle(key, response, null));
        Assertions.assertNull(key.getAttribute(CompanyAuthorizationInterceptor.OWNER_FILTER_ATTRIBUTE));
    }

    @Test
    void ownerCanReadAndWriteOwnRequest() {
        Assertions.assertTrue(handle("GET", BASE + "/1", OWNER, "oauth2", null));
        Assertions.assertTrue(handle("POST", BASE + "/1/reasons", OWNER, "oauth2", null));
        Assertions.assertTrue(handle("DELETE", BASE + "/1/reasons/2/attachments/3", OWNER, "oauth2", null));
    }

    @Test
    void nonOwnerCannotReadOrWrite() {
        Assertions.assertFalse(handle("GET", BASE + "/1", "other", "oauth2", null));
        Assertions.assertFalse(handle("PATCH", BASE + "/1/reasons/2", "other", "oauth2", null));
        Assertions.assertEquals(403, response.getStatus());
    }

    @Test
    void keyIdentityCanReadButNotWriteReasons() {
        Assertions.assertTrue(handle("GET", BASE + "/1", OWNER, "key", null));
        Assertions.assertFalse(handle("DELETE", BASE + "/1/reasons/2", OWNER, "key", null));
    }

    @Test
    void onlyKeyCanPatchStatus() {
        Assertions.assertTrue(handle("PATCH", BASE + "/1", "k", "key", null));
        Assertions.assertFalse(handle("PATCH", BASE + "/1", OWNER, "oauth2", null));
        Assertions.assertFalse(handle("PATCH", BASE + "/1/", OWNER, "oauth2", null));
    }

    @Test
    void unknownRequestIsNotFound() {
        Assertions.assertFalse(handle("GET", BASE + "/9", OWNER, "oauth2", null));
        Assertions.assertEquals(404, response.getStatus());
    }
}
