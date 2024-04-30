package com.intuit.graphql.sdk.client.config;

import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.TIME_BASED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.intuit.graphql.sdk.client.config.CircuitBreakerConfiguration;
import com.intuit.graphql.sdk.client.exceptions.GraphQLSDKHttpException;
import org.junit.jupiter.api.Test;

class CircuitBreakerConfigurationTest {

  @Test
  void testDefaultValues() {
    CircuitBreakerConfiguration circuitBreakerConfiguration = CircuitBreakerConfiguration.builder()
        .build();
    assertEquals(100, circuitBreakerConfiguration.getMinNumberOfCalls());
    assertEquals(30, circuitBreakerConfiguration.getSlowCallRatePercentage());
    assertEquals(100,
        circuitBreakerConfiguration.getPermittedCallsInHalfOpen());
    assertEquals(10, circuitBreakerConfiguration.getFailureRatePercentage());
    assertEquals(1000, circuitBreakerConfiguration.getSlidingWindowSize());
    assertEquals(8000,
        circuitBreakerConfiguration.getSlowCallDurationThresholdMs());
    assertEquals(10000,
        circuitBreakerConfiguration.getWaitDurationInOpenStateMs());
    assertEquals(TIME_BASED,
        circuitBreakerConfiguration.getSlidingWindowType());
    assertTrue(
        circuitBreakerConfiguration.getCircuitBreakerRecordingEligibilityStrategyForHttpStatusCodes()
            .isEligibleForCircuitBreakerRecording(429));
    assertFalse(
        circuitBreakerConfiguration.getCircuitBreakerRecordingEligibilityStrategyForHttpStatusCodes()
            .isEligibleForCircuitBreakerRecording(401));
    assertFalse(
        circuitBreakerConfiguration.getCircuitBreakerRecordingEligibilityStrategyForExceptions()
            .isEligibleForCircuitBreakerRecording(
                new GraphQLSDKHttpException(500, "Test Error",
                    "Some response")));
    assertNotNull(circuitBreakerConfiguration.getCircuitOpenHandler());
  }

  @Test
  void testSetValuesByBuilder() {
    CircuitBreakerConfiguration circuitBreakerConfiguration = CircuitBreakerConfiguration.builder()
        .failureRatePercentage(2)
        .slidingWindowSize(100)
        .permittedCallsInHalfOpen(2)
        .minNumberOfCalls(500)
        .failureRatePercentage(5)
        .waitDurationInOpenStateMs(200)
        .slowCallRatePercentage(20)
        .slowCallDurationThresholdMs(200)
        .build();
    assertEquals(500, circuitBreakerConfiguration.getMinNumberOfCalls());
    assertEquals(20, circuitBreakerConfiguration.getSlowCallRatePercentage());
    assertEquals(2, circuitBreakerConfiguration.getPermittedCallsInHalfOpen());
    assertEquals(5, circuitBreakerConfiguration.getFailureRatePercentage());
    assertEquals(100, circuitBreakerConfiguration.getSlidingWindowSize());
    assertEquals(200,
        circuitBreakerConfiguration.getSlowCallDurationThresholdMs());
    assertEquals(200,
        circuitBreakerConfiguration.getWaitDurationInOpenStateMs());
  }

  @Test
  void testForCustomRetryEligibilityStrategy() {
    CircuitBreakerConfiguration circuitBreakerConfiguration = CircuitBreakerConfiguration.builder()
        .circuitBreakerRecordingEligibilityStrategyForHttpStatusCodes(
            statusCode -> statusCode == 200)
        .build();
    assertTrue(
        circuitBreakerConfiguration.getCircuitBreakerRecordingEligibilityStrategyForHttpStatusCodes()
            .isEligibleForCircuitBreakerRecording(200));
    assertFalse(
        circuitBreakerConfiguration.getCircuitBreakerRecordingEligibilityStrategyForHttpStatusCodes()
            .isEligibleForCircuitBreakerRecording(201));
    assertFalse(
        circuitBreakerConfiguration.getCircuitBreakerRecordingEligibilityStrategyForHttpStatusCodes()
            .isEligibleForCircuitBreakerRecording(429));
    assertFalse(
        circuitBreakerConfiguration.getCircuitBreakerRecordingEligibilityStrategyForHttpStatusCodes()
            .isEligibleForCircuitBreakerRecording(503));

  }
}
