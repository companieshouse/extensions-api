package uk.gov.companieshouse.extensions.api.logger;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LogMethodCallAspect {

    private ApiLogger logger;

    public LogMethodCallAspect(ApiLogger logger) {
		this.logger = logger;
	}

	@Around("@annotation(LogMethodCall)")
    public Object logMethodCall(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        logger.debug("Entered " + method);
        try{
            Object proceed = joinPoint.proceed();
            logger.debug("Leaving " + method);
            return proceed;
        }catch (Throwable t) {
            logger.error(String.format("Exception during %s %s %s", joinPoint.getSignature(), t.getMessage(), t.getStackTrace()));
            throw t;
        }
    }
}
