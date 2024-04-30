package com.intuit.graphql.sdk.client.resiliency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.intuit.graphql.sdk.client.config.CircuitBreakerConfiguration;
import com.intuit.graphql.sdk.client.exceptions.GraphQLRuntimeException;
import com.intuit.graphql.sdk.client.exceptions.GraphQLSDKHttpException;
import com.intuit.graphql.sdk.client.resiliency.CircuitBreakerRegister;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import java.net.SocketTimeoutException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CircuitBreakerRegisterTest {


  @Test
  void testRegisterCircuitBreaker() {
    CircuitBreakerRegister circuitBreakerRegister = new CircuitBreakerRegister(
        CircuitBreakerConfiguration.builder().build(), "test");
    CircuitBreaker circuitBreaker = circuitBreakerRegister.getCircuitBreaker();
    assertNotNull(circuitBreaker);
    assertEquals("GraphQLSDKCircuitBreaker:test",
        circuitBreaker.getName());
    CircuitBreakerConfig circuitBreakerConfig = circuitBreaker.getCircuitBreakerConfig();
    assertEquals(1000, circuitBreakerConfig.getSlidingWindowSize());
    assertEquals(10, circuitBreakerConfig.getFailureRateThreshold());
    assertEquals(CircuitBreakerConfig.SlidingWindowType.TIME_BASED,
        circuitBreakerConfig.getSlidingWindowType());
    assertEquals(100, circuitBreakerConfig.getMinimumNumberOfCalls());
  }

  @Test
  void testGetCircuitBreakerConfig() {
    CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerRegister.getCircuitBreakerConfig(
        CircuitBreakerConfiguration.builder().build());
    assertNotNull(circuitBreakerConfig);
    assertEquals(1000, circuitBreakerConfig.getSlidingWindowSize());
    assertEquals(10, circuitBreakerConfig.getFailureRateThreshold());
    assertEquals(CircuitBreakerConfig.SlidingWindowType.TIME_BASED,
        circuitBreakerConfig.getSlidingWindowType());
    assertEquals(100, circuitBreakerConfig.getMinimumNumberOfCalls());
    assertTrue(circuitBreakerConfig.getRecordExceptionPredicate()
        .test(new SocketTimeoutException()));
    assertTrue(circuitBreakerConfig.getRecordExceptionPredicate().test(
        new GraphQLRuntimeException(new SocketTimeoutException())));
    assertFalse(circuitBreakerConfig.getRecordExceptionPredicate()
        .test(new RuntimeException()));
    assertTrue(circuitBreakerConfig.getRecordExceptionPredicate().test(
        new GraphQLSDKHttpException(429, "Too Many Requests", "")));
    assertFalse(circuitBreakerConfig.getRecordExceptionPredicate().test(
        new GraphQLSDKHttpException(401, "Unauthorized Exception",
            "")));
  }

}