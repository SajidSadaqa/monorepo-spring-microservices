package com.microservices.user.domain.events.observability;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.ObservationHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ObservationAspectTest {

  private ObservationRegistry registry;
  private RecordingHandler handler;
  private ObservationAspect aspect;

  static class RecordingHandler implements ObservationHandler<Observation.Context> {
    List<Observation.Context> contexts = new ArrayList<>();
    @Override public boolean supportsContext(Observation.Context context) { return true; }
    @Override public void onStop(Observation.Context context) { contexts.add(context); }
  }

  @BeforeEach
  void setUp() {
    registry = ObservationRegistry.create();
    handler = new RecordingHandler();
    registry.observationConfig().observationHandler(handler);
    aspect = new ObservationAspect(registry);
  }

  @Test
  void observeEndpointShouldCreateObservation() throws Throwable {
    // Arrange mock join point
    ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
    when(pjp.proceed()).thenReturn("ok");

    Method method = DummyController.class.getDeclaredMethod("dummyMethod");
    MethodSignature sig = mock(MethodSignature.class);
    when(sig.getMethod()).thenReturn(method);
    when(sig.getDeclaringType()).thenReturn(DummyController.class);
    when(sig.getName()).thenReturn("dummyMethod");
    when(pjp.getSignature()).thenReturn(sig);

    // Act
    Object result = aspect.observeEndpoint(pjp);

    // Assert
    assertThat(result).isEqualTo("ok");
    assertThat(handler.contexts).anySatisfy(ctx ->
      assertThat(ctx.getName()).contains("http.server.observe.DummyController.dummyMethod")
    );
  }

  static class DummyController {
    public String dummyMethod() { return "ok"; }
  }
}
