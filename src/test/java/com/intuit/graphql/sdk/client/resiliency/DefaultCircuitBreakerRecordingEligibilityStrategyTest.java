package com.intuit.graphql.sdk.client.resiliency;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.intuit.graphql.sdk.client.exceptions.GraphQLRuntimeException;
import com.intuit.graphql.sdk.client.resiliency.DefaultCircuitBreakerRecordingEligibilityStrategy;
import java.net.SocketTimeoutException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class DefaultCircuitBreakerRecordingEligibilityStrategyTest {

  public static Object[][] testIsEligibleForCircuitBreakerDataProvider() {
    return new Object[][]{
        {200, false},
        {201, false},
        {202, false},
        {203, false},
        {204, false},
        {205, false},
        {300, false},
        {301, false},
        {302, false},
        {303, false},
        {400, false},
        {401, false},
        {402, false},
        {403, false},
        {429, true},
        {500, true},
        {500, true},
        {503, true},
        {504, true}
    };
  }

  public static Object[][] testIsEligibleForCircuitBreakerDataProviderException() {
    return new Object[][]{
        {new SocketTimeoutException(), true},
        {new RuntimeException(), false},
        {new GraphQLRuntimeException(new SocketTimeoutException()),
            true},
    };
  }

  @ParameterizedTest
  @MethodSource("testIsEligibleForCircuitBreakerDataProvider")
  void testIsEligibleForCircuitBreaker(int statusCode,
      boolean expectedDecision) {
    DefaultCircuitBreakerRecordingEligibilityStrategy defaultCircuitBreakerRecordingEligibilityStrategy = new DefaultCircuitBreakerRecordingEligibilityStrategy();
    assertEquals(
        defaultCircuitBreakerRecordingEligibilityStrategy.isEligibleForCircuitBreakerRecording(
            statusCode), expectedDecision);
  }

  @ParameterizedTest
  @MethodSource("testIsEligibleForCircuitBreakerDataProviderException")
  void testIsEligibleForCircuitBreaker(Exception exception,
      boolean expectedDecision) {
    DefaultCircuitBreakerRecordingEligibilityStrategy defaultCircuitBreakerRecordingEligibilityStrategy = new DefaultCircuitBreakerRecordingEligibilityStrategy();
    assertEquals(
        defaultCircuitBreakerRecordingEligibilityStrategy.isEligibleForCircuitBreakerRecording(
            exception), expectedDecision);
  }
}