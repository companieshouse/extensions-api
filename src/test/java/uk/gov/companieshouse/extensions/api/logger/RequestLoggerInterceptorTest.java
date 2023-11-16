package uk.gov.companieshouse.extensions.api.logger;

import jakarta.servlet.http.HttpServletResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import uk.gov.companieshouse.extensions.api.groups.Unit;

@Category(Unit.class)
@RunWith(MockitoJUnitRunner.class)
public class RequestLoggerInterceptorTest {

    @Mock
    private ApiLogger logger;

    @InjectMocks
    private RequestLoggerInterceptor interceptor;

    private HttpServletResponse response;
    private MockHttpServletRequest request;

    @Before
    public void setup() {
        response = new MockHttpServletResponse();
        response.setStatus(200);

        request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("/company/00006400/extensions/requests");
    }

    @Test
    public void testPreHandler() {
        interceptor.preHandle(request, response, new Object());

        Mockito.verify(logger).setCompanyNumber("00006400");
        Mockito.verify(logger).info("Request received - " + request.getMethod() + " " + request.getRequestURI());
    }

    @Test
    public void testAfterCompletion_noException() {
        interceptor.afterCompletion(request, response, new Object(), null);

        Mockito.verify(logger).info("Request finished - response status = " + response.getStatus() + " - " + request.getMethod() + " " + request.getRequestURI());
        Mockito.verify(logger).removeCompanyNumber();
    }

    @Test
    public void testAfterCompletion_exception() {
        Exception e = new Exception();
        interceptor.afterCompletion(request, response, new Object(), e);

        Mockito.verify(logger).error("response status = " + response.getStatus(), e);
    }
}
