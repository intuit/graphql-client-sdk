package com.intuit.graphql.sdk.client.config;


import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.TIME_BASED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.intuit.graphql.sdk.client.config.CircuitBreakerConfiguration;
import com.intuit.graphql.sdk.client.config.ResiliencyConfiguration;
import com.intuit.graphql.sdk.client.config.ResiliencyConfiguration.ResiliencyConfigurationBuilder;
import com.intuit.graphql.sdk.client.config.RetryConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ResiliencyConfigurationTest {

  @Test
  void test() {
    ResiliencyConfiguration resiliencyConfiguration = ResiliencyConfiguration.builder()
        .retryConfig(getRetryConfiguration())
        .circuitBreakerConfig(getCircuitBreakerConfiguration())
        .build();
    assertTrue(resiliencyConfiguration.isEnabled());
    assertNotNull(resiliencyConfiguration.getRetryConfig());
    assertEquals(5, resiliencyConfiguration.getRetryConfig().getMaxAttempts());
    assertEquals(3, resiliencyConfiguration.getRetryConfig()
        .getExponentialBackOffMultiplier());
    assertEquals(50,
        resiliencyConfiguration.getRetryConfig().getRetryIntervalMillis());
    assertNotNull(resiliencyConfiguration.getCircuitBreakerConfig());
    assertEquals(30, resiliencyConfiguration.getCircuitBreakerConfig()
        .getFailureRatePercentage());
    assertEquals(25, resiliencyConfiguration.getCircuitBreakerConfig()
        .getPermittedCallsInHalfOpen());
    assertEquals(100, resiliencyConfiguration.getCircuitBreakerConfig()
        .getSlidingWindowSize());
    assertEquals(20, resiliencyConfiguration.getCircuitBreakerConfig()
        .getSlowCallRatePercentage());
    assertEquals(8000, resiliencyConfiguration.getCircuitBreakerConfig()
        .getSlowCallDurationThresholdMs());
    assertEquals(TIME_BASED, resiliencyConfiguration.getCircuitBreakerConfig()
        .getSlidingWindowType());
    assertEquals(10000, resiliencyConfiguration.getCircuitBreakerConfig()
        .getWaitDurationInOpenStateMs());
  }

  @Test
  void testWithNullRetryConfiguration() {
    ResiliencyConfigurationBuilder resiliencyConfigurationBuilder = ResiliencyConfiguration.builder();
    Assertions.assertThrows(NullPointerException.class,
        () -> resiliencyConfigurationBuilder.build());
  }

  private RetryConfiguration getRetryConfiguration() {
    return RetryConfiguration.builder()
        .exponentialBackOffMultiplier(3)
        .maxAttempts(5)
        .retryIntervalMillis(50)
        .build();
  }

  private CircuitBreakerConfiguration getCircuitBreakerConfiguration() {
    return CircuitBreakerConfiguration.builder()
        .failureRatePercentage(30)
        .permittedCallsInHalfOpen(25)
        .slidingWindowSize(100)
        .slowCallRatePercentage(20)
        .build();
  }
}
