package mops.gruppen2.aspect;

import lombok.extern.log4j.Log4j2;
import mops.gruppen2.aspect.annotation.Trace;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Log4j2
@Profile("dev")
@Aspect
@Component
public class LogAspect {


    // ######################################### POINTCUT ########################################


    @Pointcut("within(@mops.gruppen2.aspect.annotation.TraceMethodCalls *)")
    public void beanAnnotatedWithMonitor() {}

    @Pointcut("execution(public * *(..))")
    public void publicMethod() {}

    @Pointcut("publicMethod() && beanAnnotatedWithMonitor()")
    public void logMethodCalls() {}


    // ###################################### ANNOTATIONS ########################################


    @Before("@annotation(mops.gruppen2.aspect.annotation.Trace)")
    public static void logCustom(JoinPoint joinPoint) {
        log.trace(((MethodSignature) joinPoint.getSignature()).getMethod().getAnnotation(Trace.class).value());
    }

    @Before("@annotation(mops.gruppen2.aspect.annotation.TraceMethodCall) || logMethodCalls()")
    public static void logMethodCall(JoinPoint joinPoint) {
        log.trace("Methodenaufruf: {} ({})",
                  joinPoint.getSignature().getName(),
                  joinPoint.getSourceLocation().getWithinType().getName().replace("mops.gruppen2.", ""));

        System.out.println();
    }

    @Around("@annotation(mops.gruppen2.aspect.annotation.TraceExecutionTime)")
    public static Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        joinPoint.proceed();
        long stop = System.currentTimeMillis();

        log.trace("Ausführungsdauer: {} Millis", stop - start);

        return joinPoint.proceed();
    }
}
