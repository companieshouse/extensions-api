package uk.gov.companieshouse.extensions.api.logger;

import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;


@Tag("Unit")
@ExtendWith(MockitoExtension.class)
public class RequestLoggerInterceptorTest {

    @Mock
    private ApiLogger logger;

    @InjectMocks
    private RequestLoggerInterceptor interceptor;

    private HttpServletResponse response;
    private MockHttpServletRequest request;

    @BeforeEach
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
