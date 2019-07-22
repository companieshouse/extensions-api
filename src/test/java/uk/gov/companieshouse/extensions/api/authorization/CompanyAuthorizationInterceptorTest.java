package uk.gov.companieshouse.extensions.api.authorization;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import uk.gov.companieshouse.extensions.api.groups.Unit;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;

@Category(Unit.class)
@RunWith(MockitoJUnitRunner.class)
public class CompanyAuthorizationInterceptorTest {
    
    @InjectMocks
    private CompanyAuthorizationInterceptor interceptor;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ApiLogger logger;

    @Test
    public void willAuthorizeIfScopeMatchesPath() {
        when(request.getHeader("ERIC-Authorised-Scope"))
            .thenReturn("abcdefg00006400");
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("companyNumber", "00006400");
        when(request.getAttribute(anyString()))
            .thenReturn(pathParams);
        boolean result = interceptor.preHandle(request, response, null);

        assertTrue(result);
    }

    @Test
    public void willNotAuthorizeIfScopeDoesntMatchPath() {
        when(request.getHeader("ERIC-Authorised-Scope"))
            .thenReturn("abcdefg00006401");
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("companyNumber", "00006400");
        when(request.getAttribute(anyString()))
            .thenReturn(pathParams);
        boolean result = interceptor.preHandle(request, response, null);

        assertFalse(result);
        verify(response).setStatus(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void willNotAuthorizeIfScopeIsEmpty() {
        when(request.getHeader("ERIC-Authorised-Scope"))
            .thenReturn("");
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("companyNumber", "00006400");
        when(request.getAttribute(anyString()))
            .thenReturn(pathParams);
        boolean result = interceptor.preHandle(request, response, null);

        assertFalse(result);
        verify(response).setStatus(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void willAuthorizeAdminIfGetRequest() {
        when(request.getHeader("ERIC-Authorised-Roles"))
            .thenReturn("permission /admin/extensions-view anotherPermission");
        when(request.getHeader("ERIC-Authorised-Scope")) 
            .thenReturn("");
        when(request.getRequestURI()) 
            .thenReturn("");
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("companyNumber", "00006400");
        when(request.getAttribute(anyString()))
            .thenReturn(pathParams);
        when(request.getMethod())
            .thenReturn("GET");
        boolean result = interceptor.preHandle(request, response, null);

        assertTrue(result);
        verify(request, times(2)).getHeader("ERIC-Authorised-Roles");
    }

    @Test
    public void willAuthorizeAdminIfDownloadGetRequest() {
        when(request.getHeader("ERIC-Authorised-Roles"))
            .thenReturn("permission /admin/extensions-view /admin/extensions-download");
        when(request.getHeader("ERIC-Authorised-Scope")) 
            .thenReturn("");
        when(request.getRequestURI()) 
            .thenReturn("download");
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("companyNumber", "00006400");
        when(request.getAttribute(anyString()))
            .thenReturn(pathParams);
        when(request.getMethod())
            .thenReturn("GET");
        boolean result = interceptor.preHandle(request, response, null);

        assertTrue(result);
        verify(request, times(2)).getHeader("ERIC-Authorised-Roles");
    }

    @Test
    public void willNotAuthorizeAdminIfDownloadGetRequestWithoutView() {
        when(request.getHeader("ERIC-Authorised-Roles"))
            .thenReturn("permission /admin/extensions-download");
        when(request.getHeader("ERIC-Authorised-Scope")) 
            .thenReturn("");
        when(request.getRequestURI()) 
            .thenReturn("download");
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("companyNumber", "00006400");
        when(request.getAttribute(anyString()))
            .thenReturn(pathParams);
        when(request.getMethod())
            .thenReturn("GET");
        boolean result = interceptor.preHandle(request, response, null);

        assertFalse(result);
        verify(request, times(2)).getHeader("ERIC-Authorised-Roles");
    }

    @Test
    public void willNotAuthorizeAdminIfDownloadGetRequestWithoutDownload() {
        when(request.getHeader("ERIC-Authorised-Roles"))
            .thenReturn("permission /admin/extensions-view");
        when(request.getHeader("ERIC-Authorised-Scope")) 
            .thenReturn("");
        when(request.getRequestURI()) 
            .thenReturn("download");
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("companyNumber", "00006400");
        when(request.getAttribute(anyString()))
            .thenReturn(pathParams);
        when(request.getMethod())
            .thenReturn("GET");
        boolean result = interceptor.preHandle(request, response, null);

        assertFalse(result);
        verify(request, times(2)).getHeader("ERIC-Authorised-Roles");
    }

    @Test
    public void willNotAuthorizeAdminIfPostRequest() {
        when(request.getHeader("ERIC-Authorised-Scope")) 
            .thenReturn("");
        Map<String, String> pathParams = new HashMap<>();
        pathParams.put("companyNumber", "00006400");
        when(request.getAttribute(anyString()))
            .thenReturn(pathParams);
        when(request.getMethod())
            .thenReturn("POST");
        boolean result = interceptor.preHandle(request, response, null);

        assertFalse(result);
        verify(request, never()).getHeader("ERIC-Authorised-Roles");
        verify(response).setStatus(HttpStatus.SC_UNAUTHORIZED);
    }
}