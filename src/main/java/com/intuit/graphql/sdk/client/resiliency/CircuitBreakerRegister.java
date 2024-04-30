package com.intuit.graphql.sdk.client.resiliency;

import com.intuit.graphql.sdk.client.config.CircuitBreakerConfiguration;
import com.intuit.graphql.sdk.client.exceptions.GraphQLSDKHttpException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.time.Duration;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CircuitBreakerRegister {

  @Getter
  private final CircuitBreaker circuitBreaker;
  private static final Logger LOG = LoggerFactory.getLogger(
      CircuitBreakerRegister.class);

  public CircuitBreakerRegister(
      CircuitBreakerConfiguration circuitBreakerConfiguration,
      String endpoint) {
    CircuitBreakerConfig circuitBreakerConfig = getCircuitBreakerConfig(
        circuitBreakerConfiguration);

    CircuitBreakerRegistry circuitBreakerRegistry =
        CircuitBreakerRegistry.of(circuitBreakerConfig);

    circuitBreakerRegistry.getEventPublisher()
        .onEntryAdded(entryAddedEvent -> {
          CircuitBreaker addedCircuitBreaker = entryAddedEvent.getAddedEntry();
          LOG.info("CircuitBreaker {} added", addedCircuitBreaker.getName());
        })
        .onEntryRemoved(entryRemovedEvent -> {
          CircuitBreaker removedCircuitBreaker = entryRemovedEvent.getRemovedEntry();
          LOG.info("CircuitBreaker {} removed",
              removedCircuitBreaker.getName());
        });

    circuitBreaker = circuitBreakerRegistry
        .circuitBreaker("GraphQLSDKCircuitBreaker:" + endpoint,
            circuitBreakerConfig);

    circuitBreaker.getEventPublisher()
        .onSuccess(event -> LOG.debug(event.toString()))
        .onError(event -> LOG.info(event.toString()))
        .onCallNotPermitted(event -> LOG.info(event.toString()))
        .onIgnoredError(event -> LOG.debug(event.toString()))
        .onReset(event -> LOG.info(event.toString()))
        .onStateTransition(event -> LOG.info(event.toString()));

  }

  static CircuitBreakerConfig getCircuitBreakerConfig(
      CircuitBreakerConfiguration circuitBreakerConfiguration) {
    return CircuitBreakerConfig.custom()
        .failureRateThreshold(
            circuitBreakerConfiguration.getFailureRatePercentage())
        .slowCallRateThreshold(
            circuitBreakerConfiguration.getSlowCallRatePercentage())
        .waitDurationInOpenState(Duration.ofMillis(
            circuitBreakerConfiguration.getWaitDurationInOpenStateMs()))
        .slowCallDurationThreshold(Duration.ofMillis(
            circuitBreakerConfiguration.getSlowCallDurationThresholdMs()))
        .permittedNumberOfCallsInHalfOpenState(
            circuitBreakerConfiguration.getPermittedCallsInHalfOpen())
        .minimumNumberOfCalls(circuitBreakerConfiguration.getMinNumberOfCalls())
        .slidingWindowType(circuitBreakerConfiguration.getSlidingWindowType())
        .slidingWindowSize(circuitBreakerConfiguration.getSlidingWindowSize())
        .recordException(exception -> {
          if ((exception instanceof GraphQLSDKHttpException)) {
            return circuitBreakerConfiguration.getCircuitBreakerRecordingEligibilityStrategyForHttpStatusCodes()
                .isEligibleForCircuitBreakerRecording(
                    ((GraphQLSDKHttpException) exception).getStatusCode());
          } else {
            return circuitBreakerConfiguration.getCircuitBreakerRecordingEligibilityStrategyForExceptions()
                .isEligibleForCircuitBreakerRecording((Exception) exception);
          }
        })
        .build();
  }

}
