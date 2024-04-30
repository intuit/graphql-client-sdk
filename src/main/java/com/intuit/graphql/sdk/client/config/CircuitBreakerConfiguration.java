package com.intuit.graphql.sdk.client.config;

import com.intuit.graphql.sdk.client.resiliency.CircuitBreakerRecordingEligibilityStrategyForExceptions;
import com.intuit.graphql.sdk.client.resiliency.CircuitBreakerRecordingEligibilityStrategyForHttpStatusCodes;
import com.intuit.graphql.sdk.client.resiliency.CircuitOpenHandler;
import com.intuit.graphql.sdk.client.resiliency.DefaultCircuitBreakerRecordingEligibilityStrategy;
import com.intuit.graphql.sdk.client.resiliency.DefaultCircuitOpenHandler;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CircuitBreakerConfiguration {

  @Builder.Default
  private CircuitBreakerConfig.SlidingWindowType slidingWindowType = CircuitBreakerConfig.SlidingWindowType.TIME_BASED;
  @Builder.Default
  private int slidingWindowSize = 1000;
  @Builder.Default
  private int failureRatePercentage = 10;
  @Builder.Default
  private int slowCallRatePercentage = 30;
  @Builder.Default
  private int slowCallDurationThresholdMs = 8000;
  @Builder.Default
  private int permittedCallsInHalfOpen = 100;
  @Builder.Default
  private int minNumberOfCalls = 100;
  @Builder.Default
  private int waitDurationInOpenStateMs = 10000;
  @Builder.Default
  private CircuitBreakerRecordingEligibilityStrategyForHttpStatusCodes circuitBreakerRecordingEligibilityStrategyForHttpStatusCodes = new DefaultCircuitBreakerRecordingEligibilityStrategy();
  @Builder.Default
  private CircuitBreakerRecordingEligibilityStrategyForExceptions circuitBreakerRecordingEligibilityStrategyForExceptions = new DefaultCircuitBreakerRecordingEligibilityStrategy();
  @Builder.Default
  private CircuitOpenHandler circuitOpenHandler = new DefaultCircuitOpenHandler();

}
