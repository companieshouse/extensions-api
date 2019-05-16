package uk.gov.companieshouse.extensions.api.logger;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import javax.servlet.http.HttpServletResponse;

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
        request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("some uri");
    }

    @Test
    public void testPreHandler() {
        interceptor.preHandle(request, response, new Object());

        Mockito.verify(logger).info("Request received - " + request.getMethod() + " " + request.getRequestURI());
    }

    @Test
    public void testAfterCompletion_noException() {
        interceptor.afterCompletion(request, response, new Object(), null);

        Mockito.verify(logger).info("Request finished - " + request.getMethod() + " " + request.getRequestURI());
    }

    @Test
    public void testAfterCompletion_exception() {
        Exception e = new Exception();
        interceptor.afterCompletion(request, response, new Object(), e);

        Mockito.verify(logger).error(e);
    }
}
