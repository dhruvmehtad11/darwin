package darwincatalog.annotation;

import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.event.Level;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class TimedAspect {

  @Around("@annotation(Timed)")
  public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
    long start = System.currentTimeMillis();
    try {
      return joinPoint.proceed();
    } finally {
      long end = System.currentTimeMillis();
      MethodSignature signature = (MethodSignature) joinPoint.getSignature();
      String methodName = signature.toShortString();
      Method method = signature.getMethod();
      Timed timed = method.getAnnotation(Timed.class);
      Level logLevel = timed.logLevel();
      String message = String.format("[TIMED] %s executed in %s ms", methodName, (end - start));
      switch (logLevel) {
        case DEBUG:
          log.debug(message);
          break;
        case INFO:
        case WARN:
        case ERROR:
        default:
          log.info(message);
          break;
      }
    }
  }
}
