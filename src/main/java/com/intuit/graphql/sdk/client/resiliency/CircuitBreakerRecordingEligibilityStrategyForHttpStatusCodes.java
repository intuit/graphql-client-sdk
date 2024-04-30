package com.intuit.graphql.sdk.client.resiliency;

public interface CircuitBreakerRecordingEligibilityStrategyForHttpStatusCodes {

  boolean isEligibleForCircuitBreakerRecording(int statusCode);


}
