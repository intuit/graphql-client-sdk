package com.intuit.graphql.sdk.client.resiliency;

public interface CircuitBreakerRecordingEligibilityStrategyForExceptions {

  boolean isEligibleForCircuitBreakerRecording(Exception e);

}
