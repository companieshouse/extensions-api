package uk.gov.companieshouse.extensions.api.logger;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
public class LogMethodCallAspectTest {

    @Mock
    private ApiLogger apiLogger;

    @InjectMocks
    private LogMethodCallAspect logMethodCallAspect;

    @Test
    public void testLogMethodCall() throws Throwable {
        ProceedingJoinPoint joinPoint = Mockito.mock(ProceedingJoinPoint.class);
        MethodSignature methodSignature = Mockito.mock(MethodSignature.class);
        Method method = Mockito.mock(Method.class);

        Mockito.when(joinPoint.getSignature()).thenReturn(methodSignature);
        Mockito.when(methodSignature.getMethod()).thenReturn(method);

        logMethodCallAspect.logMethodCall(joinPoint);

        Mockito.verify(apiLogger).debug("Entered " + method);
        Mockito.verify(joinPoint).proceed();
        Mockito.verify(apiLogger).debug("Leaving " + method);
    }
}
