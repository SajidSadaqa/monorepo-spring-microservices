package com.microservices.user.domain.events.observability; // change to admin package in admin-service

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.Optional;

@Slf4j
@Aspect
@Component
public class ObservationAspect {

  private final ObservationRegistry registry;

  // Change hard-coded value to "admin-service" in the admin module
  private static final String SERVICE_NAME = "user-service";

  public ObservationAspect(ObservationRegistry registry) {
    this.registry = registry;
  }

  // Observe all methods in classes annotated with @RestController
  @Around("within(@org.springframework.web.bind.annotation.RestController *)")
  public Object observeEndpoint(ProceedingJoinPoint pjp) throws Throwable {
    MethodSignature sig = (MethodSignature) pjp.getSignature();
    Method method = sig.getMethod();
    String controller = sig.getDeclaringType().getSimpleName();
    String methodName = method.getName();
    String path = extractFirstPathFromRequestMapping(sig.getDeclaringType(), method).orElse("n/a");

    String obsName = "http.server.observe." + controller + "." + methodName;
    Observation obs = Observation.createNotStarted(obsName, registry)
      .lowCardinalityKeyValue("service", SERVICE_NAME)
      .lowCardinalityKeyValue("controller", controller)
      .lowCardinalityKeyValue("method", methodName)
      .lowCardinalityKeyValue("path", path)
      .contextualName(controller + "#" + methodName);

    obs.start();
    try {
      Object result = pjp.proceed();
      // Custom success event you can filter on
      obs.event(Observation.Event.of("success"));
      return result;
    } catch (Throwable ex) {
      obs.error(ex); // will trigger handler.onError
      throw ex;
    } finally {
      obs.stop();    // will trigger handler.onStop
    }
  }

  private Optional<String> extractFirstPathFromRequestMapping(Class<?> type, Method method) {
    // Try method-level mapping first
    RequestMapping m = AnnotationUtils.findAnnotation(method, RequestMapping.class);
    if (m != null && m.value().length > 0) return Optional.of(m.value()[0]);

    // Fallback to class-level mapping
    RequestMapping c = AnnotationUtils.findAnnotation(type, RequestMapping.class);
    if (c != null && c.value().length > 0) return Optional.of(c.value()[0]);

    return Optional.empty();
  }
}
