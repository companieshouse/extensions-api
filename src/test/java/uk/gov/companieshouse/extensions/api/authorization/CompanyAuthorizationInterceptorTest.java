package uk.gov.companieshouse.extensions.api.authorization;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.extensions.api.logger.ApiLogger;

import static org.mockito.Mockito.*;

@Tag("UnitTest")
@ExtendWith(MockitoExtension.class)
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
    public void willAuthorizeAdminIfGetRequest() {
        when(request.getHeader("ERIC-Authorised-Roles"))
            .thenReturn("permission /admin/extensions-view anotherPermission");
        when(request.getRequestURI())
            .thenReturn("");
        when(request.getMethod())
            .thenReturn("GET");
        boolean result = interceptor.preHandle(request, response, null);

        Assertions.assertTrue(result);
        verify(request).getHeader("ERIC-Authorised-Roles");
    }

    @Test
    public void willAuthorizeAdminIfDownloadGetRequest() {
        when(request.getHeader("ERIC-Authorised-Roles"))
            .thenReturn("permission /admin/extensions-view /admin/extensions-download");
        when(request.getRequestURI())
            .thenReturn("download");
        when(request.getMethod())
            .thenReturn("GET");
        boolean result = interceptor.preHandle(request, response, null);

        Assertions.assertTrue(result);
        verify(request, times(2)).getHeader("ERIC-Authorised-Roles");
    }

    @Test
    public void willNotAuthorizeAdminIfDownloadGetRequestWithoutView() {
        when(request.getHeader("ERIC-Authorised-Roles"))
            .thenReturn("permission /admin/extensions-download");
        when(request.getRequestURI())
            .thenReturn("download");
        when(request.getMethod())
            .thenReturn("GET");
        boolean result = interceptor.preHandle(request, response, null);

        Assertions.assertFalse(result);
        verify(request, times(2)).getHeader("ERIC-Authorised-Roles");
    }

    @Test
    public void willNotAuthorizeAdminIfDownloadGetRequestWithoutDownload() {
        when(request.getHeader("ERIC-Authorised-Roles"))
            .thenReturn("permission /admin/extensions-view");
        when(request.getRequestURI())
            .thenReturn("download");
        when(request.getMethod())
            .thenReturn("GET");
        boolean result = interceptor.preHandle(request, response, null);

        Assertions.assertFalse(result);
        verify(request, times(2)).getHeader("ERIC-Authorised-Roles");
    }

    @Test
    public void willNotAuthorizeAdminIfPostRequest() {
        when(request.getHeader("ERIC-Authorised-Roles"))
            .thenReturn("/admin/extensions-view");
        when(request.getMethod())
            .thenReturn("POST");
        boolean result = interceptor.preHandle(request, response, null);

        Assertions.assertFalse(result);
        verify(request).getHeader("ERIC-Authorised-Roles");
        verify(response).setStatus(HttpStatus.SC_UNAUTHORIZED);
    }

    @Test
    public void willAuthorizeUserToPost() {
        when(request.getMethod())
            .thenReturn("POST");
        boolean result = interceptor.preHandle(request, response, null);

        Assertions.assertTrue(result);
        verify(request).getHeader("ERIC-Authorised-Roles");
    }

    @Test
    public void willNotAuthoriseUserToDownload() {
        when(request.getMethod())
            .thenReturn("GET");
        boolean result = interceptor.preHandle(request, response, null);

        Assertions.assertFalse(result);
        verify(request).getHeader("ERIC-Authorised-Roles");
    }
}
