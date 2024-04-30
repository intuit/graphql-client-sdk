package com.intuit.graphql.sdk.client.resiliency;

import java.io.IOException;

public class DefaultCircuitBreakerRecordingEligibilityStrategy implements
    CircuitBreakerRecordingEligibilityStrategyForHttpStatusCodes,
    CircuitBreakerRecordingEligibilityStrategyForExceptions {

  public static final int THROTTLING_STATUS_CODE = 429;

  @Override
  public boolean isEligibleForCircuitBreakerRecording(int statusCode) {
    return statusCode == THROTTLING_STATUS_CODE
        || isServerError(statusCode);
  }

  private static boolean isServerError(int statusCode) {
    return statusCode >= 500 && statusCode <= 599;
  }

  @Override
  public boolean isEligibleForCircuitBreakerRecording(Exception e) {
    return e instanceof IOException || e.getCause() instanceof IOException;
  }

}
