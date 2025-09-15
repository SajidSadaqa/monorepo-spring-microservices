package com.microservices.user.domain.events.observability;

import io.micrometer.observation.Observation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class LoggingObservationHandlerTest {

  private LoggingObservationHandler handler;
  private Observation.Context context;

  @BeforeEach
  void setUp() {
    handler = new LoggingObservationHandler();
    context = new Observation.Context();
    context.setName("test-obs");
  }

  @Test
  void supportsContextShouldAlwaysReturnTrue() {
    assert handler.supportsContext(context);
  }

  @Test
  void onStartShouldLog() {
    handler.onStart(context); // no exception expected
  }

  @Test
  void onEventShouldLog() {
    Observation.Event event = Observation.Event.of("success");
    handler.onEvent(event, context); // no exception expected
  }

  @Test
  void onErrorShouldLog() {
    context.setError(new RuntimeException("boom"));
    handler.onError(context); // no exception expected
  }

  @Test
  void onStopShouldLog() {
    handler.onStop(context); // no exception expected
  }
}
