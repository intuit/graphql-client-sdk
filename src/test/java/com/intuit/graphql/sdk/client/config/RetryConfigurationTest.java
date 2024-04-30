package com.intuit.graphql.sdk.client.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.intuit.graphql.sdk.client.config.RetryConfiguration;
import com.intuit.graphql.sdk.client.exceptions.GraphQLSDKHttpException;
import org.junit.jupiter.api.Test;

class RetryConfigurationTest {

  @Test
  void testDefaultValues() {
    RetryConfiguration retryConfiguration = RetryConfiguration.builder()
        .build();
    assertEquals(2, retryConfiguration.getMaxAttempts());
    assertEquals(50, retryConfiguration.getRetryIntervalMillis());
    assertEquals(1, retryConfiguration.getExponentialBackOffMultiplier());
    assertTrue(
        retryConfiguration.getRetryEligibilityStrategyForHttpStatusCodes()
            .isEligibleForRetry(429));
    assertFalse(
        retryConfiguration.getRetryEligibilityStrategyForHttpStatusCodes()
            .isEligibleForRetry(401));
    assertFalse(retryConfiguration.getRetryEligibilityStrategyForExceptions()
        .isEligibleForRetry(
            new GraphQLSDKHttpException(500, "Test Error",
                "Some response")));
    assertNotNull(retryConfiguration.getIntervalFunction());
  }

  @Test
  void testSetValuesByBuilder() {
    RetryConfiguration retryConfiguration = RetryConfiguration.builder()
        .exponentialBackOffMultiplier(2)
        .maxAttempts(100)
        .retryIntervalMillis(10)
        .build();
    assertEquals(100, retryConfiguration.getMaxAttempts());
    assertEquals(10, retryConfiguration.getRetryIntervalMillis());
    assertEquals(2, retryConfiguration.getExponentialBackOffMultiplier());
  }

  @Test
  void testForCustomRetryEligibilityStrategy() {
    RetryConfiguration retryConfiguration = RetryConfiguration.builder()
        .retryEligibilityStrategyForHttpStatusCodes(
            statusCode -> statusCode == 200)
        .build();
    assertTrue(
        retryConfiguration.getRetryEligibilityStrategyForHttpStatusCodes()
            .isEligibleForRetry(200));
    assertFalse(
        retryConfiguration.getRetryEligibilityStrategyForHttpStatusCodes()
            .isEligibleForRetry(201));
    assertFalse(
        retryConfiguration.getRetryEligibilityStrategyForHttpStatusCodes()
            .isEligibleForRetry(429));
    assertFalse(
        retryConfiguration.getRetryEligibilityStrategyForHttpStatusCodes()
            .isEligibleForRetry(503));

  }

}