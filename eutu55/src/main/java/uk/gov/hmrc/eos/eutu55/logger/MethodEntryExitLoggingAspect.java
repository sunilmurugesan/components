package uk.gov.hmrc.eos.eutu55.logger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import uk.gov.hmrc.eos.eutu55.entity.SubscriptionStatus;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class MethodEntryExitLoggingAspect {

  private final LoggerComponent loggerComponent;

  /**
   * AOP expression for controller components method execution time
   * Log entry point for POST request is done by {@link RequestBodyLoggerAdvice}
   * Log entry point for GET request is done by this method.
   */
  @Around("execution(* uk.gov.hmrc.eos.eutu55.controller..*(..)))")
  public Object profileAllControllerMethods(ProceedingJoinPoint proceedingJoinPoint)
      throws Throwable {
    logEntryForGetRequest(proceedingJoinPoint);
    return logMethodExecutionTime(proceedingJoinPoint);
  }

  //AOP expression for service components method execution time
  @Around("execution(* uk.gov.hmrc.eos.eutu55.service.GatewayService.*(..)))")
  public Object profileGatewayServiceMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
    loggerComponent.apiCall();
    Object result = proceedingJoinPoint.proceed();
    loggerComponent.logAPICallResponse(result);
    return result;
  }

    @Around("execution(* uk.gov.hmrc.eos.eutu55.repository.SubscriptionStatusRepository.save(..)) && args(subscriptionStatus, ..))")
    public Object profileSubscriptionStatusRepositorySaveMethod(ProceedingJoinPoint proceedingJoinPoint, SubscriptionStatus subscriptionStatus)
            throws Throwable {
        loggerComponent.persistRecord();
        Object result = logMethodExecutionTime(proceedingJoinPoint);
        log.info("Updating Subscription Status to {}", subscriptionStatus.getStatus());
        loggerComponent.clearEventType();
        return result;
    }

  @Around("execution(* uk.gov.hmrc.eos.eutu55.repository.AdminAuditRepository.save(..)))")
  public Object profileAdminAuditRepositorySaveMethod(ProceedingJoinPoint proceedingJoinPoint)
          throws Throwable {
    loggerComponent.persistRecord();
    Object result = logMethodExecutionTime(proceedingJoinPoint);
    log.info("Created Admin Audit record");
    loggerComponent.clearEventType();
    return result;
  }

  //AOP expression for dao components method execution time
  @Around("execution(* uk.gov.hmrc.eos.eutu55.dao.SynchronisationDao.save(..))) &&" +
          "execution(* uk.gov.hmrc.eos.eutu55.dao.SynchronisationDao.update(..))) &&" +
          "execution(* uk.gov.hmrc.eos.eutu55.dao.SynchronisationDao.delete(..)))")
  public Object profileSynchronisationDaoMethods(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
    loggerComponent.persistRecord();
    return logMethodExecutionTime(proceedingJoinPoint);
  }

  //AOP expression for dao components method execution time
  @Around("execution(* uk.gov.hmrc.eos.eutu55.dao.SynchronisationAuditDao.save(..)))")
  public Object profileSynchronisationAuditDaoSaveMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
    loggerComponent.persistRecord();
    return logMethodExecutionTime(proceedingJoinPoint);
  }

  @Around("execution(* uk.gov.hmrc.eos.eutu55.ui..*(..)))")
  public Object profileAllUiMethods(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
    return logMethodExecutionTime(proceedingJoinPoint);
  }

  @Around("execution(* uk.gov.hmrc.eos.eutu55.exception..*(..)))")
  public Object profileExceptionHandlers(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
    return proceedingJoinPoint.proceed();
  }

  @Around("execution(* uk.gov.hmrc.eos.eutu55.housekeeping..*(..)))")
  public Object profileAllHouseKeepingMethods(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
    return logMethodExecutionTime(proceedingJoinPoint);
  }

  private Object logMethodExecutionTime(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
    MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();

    //Get intercepted method details
    String className = methodSignature.getDeclaringType().getSimpleName();
    String methodName = methodSignature.getName();

    final StopWatch stopWatch = new StopWatch();

    //Measure method execution time
    stopWatch.start();
    log.debug("Entry Point: " + className + "." + methodName +" Started");
    Object result = proceedingJoinPoint.proceed();
    stopWatch.stop();
    //Log method execution time
    log.debug("Exit Point: " + className + "." + methodName + " :: " + stopWatch
        .getTotalTimeMillis() + " ms Completed");
    return result;
  }

    private void logEntryForGetRequest(ProceedingJoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        if (method.getAnnotation(GetMapping.class) != null) {
            loggerComponent.entryPoint();
        }
    }
}
