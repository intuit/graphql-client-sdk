package com.intuit.graphql.sdk.client.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.intuit.graphql.sdk.client.config.CircuitBreakerConfiguration;
import com.intuit.graphql.sdk.client.config.GraphQLClientConfiguration;
import com.intuit.graphql.sdk.client.config.GraphQLClientConfiguration.GraphQLClientConfigurationBuilder;
import com.intuit.graphql.sdk.client.config.HttpConfiguration;
import com.intuit.graphql.sdk.client.config.ResiliencyConfiguration;
import com.intuit.graphql.sdk.client.config.RetryConfiguration;
import com.intuit.graphql.sdk.client.resiliency.CircuitBreakerRecordingEligibilityStrategyForExceptions;
import com.intuit.graphql.sdk.client.resiliency.CircuitBreakerRecordingEligibilityStrategyForHttpStatusCodes;
import com.intuit.graphql.sdk.client.resiliency.DefaultCircuitBreakerRecordingEligibilityStrategy;
import com.intuit.graphql.sdk.client.resiliency.DefaultRetryEligibilityStrategy;
import com.intuit.graphql.sdk.client.resiliency.RetryEligibilityStrategyForExceptions;
import com.intuit.graphql.sdk.client.resiliency.RetryEligibilityStrategyForHttpStatusCodes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GraphQLClientConfigurationTest {

  private final RetryEligibilityStrategyForExceptions retryEligibilityStrategyForExceptions = new DefaultRetryEligibilityStrategy();
  private final RetryEligibilityStrategyForHttpStatusCodes retryEligibilityStrategyForHttpStatusCodes = new DefaultRetryEligibilityStrategy();
  private final CircuitBreakerRecordingEligibilityStrategyForExceptions circuitBreakerRecordingEligibilityStrategyForExceptions = new DefaultCircuitBreakerRecordingEligibilityStrategy();
  private final CircuitBreakerRecordingEligibilityStrategyForHttpStatusCodes circuitBreakerRecordingEligibilityStrategyForHttpStatusCodes = new DefaultCircuitBreakerRecordingEligibilityStrategy();

  @Test
  void test() {
    GraphQLClientConfiguration configuration = GraphQLClientConfiguration
        .builder()
        .graphQLApiUrl("http://example.org/graphql")
        .httpConfiguration(
            HttpConfiguration.builder()
                .maxTotalConnections(100)
                .connectTimeoutMilliSeconds(1000)
                .connectionRequestTimeoutMilliSeconds(500)
                .maxConnectionsPerRoute(10)
                .maxConnectionIdleTimeSeconds(60)
                .socketTimeOutMilliSeconds(2000)
                .connectionKeepLiveTimeSeconds(120)
                .build()
        ).resilienceConfiguration(ResiliencyConfiguration.builder()
            .retryConfig(
                RetryConfiguration.builder()
                    .maxAttempts(4)
                    .exponentialBackOffMultiplier(3)
                    .retryIntervalMillis(100)
                    .retryEligibilityStrategyForHttpStatusCodes(
                        retryEligibilityStrategyForHttpStatusCodes)
                    .retryEligibilityStrategyForExceptions(
                        retryEligibilityStrategyForExceptions)
                    .build()

            )
            .circuitBreakerConfig(
                CircuitBreakerConfiguration.builder()
                    .minNumberOfCalls(2000)
                    .failureRatePercentage(35)
                    .waitDurationInOpenStateMs(200)
                    .permittedCallsInHalfOpen(20)
                    .slowCallRatePercentage(10)
                    .circuitBreakerRecordingEligibilityStrategyForHttpStatusCodes(
                        circuitBreakerRecordingEligibilityStrategyForHttpStatusCodes)
                    .circuitBreakerRecordingEligibilityStrategyForExceptions(
                        circuitBreakerRecordingEligibilityStrategyForExceptions)
                    .build()
            )
            .build())
        .build();
    assertEquals("http://example.org/graphql",
        configuration.getGraphQLApiUrl());
    assertEquals(100,
        configuration.getHttpConfiguration().getMaxTotalConnections());
    assertEquals(1000,
        configuration.getHttpConfiguration().getConnectTimeoutMilliSeconds());
    assertEquals(500, configuration.getHttpConfiguration()
        .getConnectionRequestTimeoutMilliSeconds());
    assertEquals(10,
        configuration.getHttpConfiguration().getMaxConnectionsPerRoute());
    assertEquals(60,
        configuration.getHttpConfiguration().getMaxConnectionIdleTimeSeconds());
    assertEquals(2000,
        configuration.getHttpConfiguration().getSocketTimeOutMilliSeconds());
    assertEquals(120, configuration.getHttpConfiguration()
        .getConnectionKeepLiveTimeSeconds());
    assertEquals(4, configuration.getResilienceConfiguration().getRetryConfig()
        .getMaxAttempts());
    assertEquals(3, configuration.getResilienceConfiguration().getRetryConfig()
        .getExponentialBackOffMultiplier());
    assertEquals(100,
        configuration.getResilienceConfiguration().getRetryConfig()
            .getRetryIntervalMillis());
    assertEquals(retryEligibilityStrategyForHttpStatusCodes,
        configuration.getResilienceConfiguration().getRetryConfig()
            .getRetryEligibilityStrategyForHttpStatusCodes());
    assertEquals(retryEligibilityStrategyForExceptions,
        configuration.getResilienceConfiguration().getRetryConfig()
            .getRetryEligibilityStrategyForExceptions());
    assertEquals(2000,
        configuration.getResilienceConfiguration().getCircuitBreakerConfig()
            .getMinNumberOfCalls());
    assertEquals(35,
        configuration.getResilienceConfiguration().getCircuitBreakerConfig()
            .getFailureRatePercentage());
    assertEquals(200,
        configuration.getResilienceConfiguration().getCircuitBreakerConfig()
            .getWaitDurationInOpenStateMs());
    assertEquals(20,
        configuration.getResilienceConfiguration().getCircuitBreakerConfig()
            .getPermittedCallsInHalfOpen());
    assertEquals(10,
        configuration.getResilienceConfiguration().getCircuitBreakerConfig()
            .getSlowCallRatePercentage());
    assertEquals(circuitBreakerRecordingEligibilityStrategyForHttpStatusCodes,
        configuration.getResilienceConfiguration().getCircuitBreakerConfig()
            .getCircuitBreakerRecordingEligibilityStrategyForHttpStatusCodes());
    assertEquals(circuitBreakerRecordingEligibilityStrategyForExceptions,
        configuration.getResilienceConfiguration().getCircuitBreakerConfig()
            .getCircuitBreakerRecordingEligibilityStrategyForExceptions());
  }

  @Test
  void testWithNullHttpConfiguration() {
    GraphQLClientConfigurationBuilder graphQLConfigurationBuilder = GraphQLClientConfiguration.builder();
    Assertions.assertThrows(NullPointerException.class,
        graphQLConfigurationBuilder::build);
  }

}