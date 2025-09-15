package com.microservices.user.domain.events.observability;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Emits start/stop/error and custom events (e.g., "success") to your logs.
 * You can later swap this to publish to Kafka/OTel, write to DB, etc.
 */
@Component
public class LoggingObservationHandler implements ObservationHandler<Observation.Context> {

  private static final Logger log = LoggerFactory.getLogger(LoggingObservationHandler.class);

  @Override
  public boolean supportsContext(Observation.Context context) {
    return true; // handle all observations
  }

  @Override
  public void onStart(Observation.Context context) {
    log.info("OBSERVE_START name={} tags={}", context.getName(), context.getLowCardinalityKeyValues());
  }

  @Override
  public void onEvent(Observation.Event event, Observation.Context context) {
    log.info("OBSERVE_EVENT  name={} event={} tags={}", context.getName(), event.getName(),
      context.getLowCardinalityKeyValues());
  }

  @Override
  public void onError(Observation.Context context) {
    log.error("OBSERVE_ERROR  name={} ex={}", context.getName(),
      context.getError() != null ? context.getError().toString() : "n/a");
  }

  @Override
  public void onStop(Observation.Context context) {
    log.info("OBSERVE_STOP  name={} tags={}", context.getName(), context.getLowCardinalityKeyValues());
  }
}
