package uk.gov.companieshouse.extensions.api.logger;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
public class LogMethodCallAspect {

    @Autowired
    private ApiLogger logger;

    @Around("@annotation(LogMethodCall)")
    public Object logMethodCall(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        logger.debug("Entered " + method);

        Object proceed = joinPoint.proceed();

        logger.debug("Leaving " + method);
        return proceed;
    }
}
