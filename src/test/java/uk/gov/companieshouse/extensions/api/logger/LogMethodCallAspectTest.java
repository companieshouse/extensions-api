package uk.gov.companieshouse.extensions.api.logger;

import static org.mockito.Mockito.mock;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

@Tag("UnitTest")
public class LogMethodCallAspectTest {

    @Mock
    private ApiLogger apiLogger = mock(ApiLogger.class);

    private LogMethodCallAspect logMethodCallAspect = new LogMethodCallAspect(apiLogger);

    @Test
    public void testLogMethodCall() throws Throwable {
        ProceedingJoinPoint joinPoint = Mockito.mock(ProceedingJoinPoint.class);
        MethodSignature methodSignature = Mockito.mock(MethodSignature.class);

        Mockito.when(joinPoint.getSignature()).thenReturn(methodSignature);

        logMethodCallAspect.logMethodCall(joinPoint);

        Mockito.verify(apiLogger).debug("Entered null");
        Mockito.verify(joinPoint).proceed();
        Mockito.verify(apiLogger).debug("Leaving null");
    }

    @Test
    public void testLogMethodCallException() throws Throwable {
        ProceedingJoinPoint joinPoint = Mockito.mock(ProceedingJoinPoint.class);
        MethodSignature methodSignature = Mockito.mock(MethodSignature.class);

        Mockito.when(joinPoint.getSignature()).thenReturn(methodSignature);

        Mockito.when(joinPoint.proceed()).thenThrow(new RuntimeException("Test exception"));

        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class, () -> {
            logMethodCallAspect.logMethodCall(joinPoint);
        });

        Assertions.assertEquals("Test exception", thrown.getMessage());
    }
}
