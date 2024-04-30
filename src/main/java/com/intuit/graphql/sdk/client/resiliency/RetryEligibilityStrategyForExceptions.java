package com.intuit.graphql.sdk.client.resiliency;

public interface RetryEligibilityStrategyForExceptions {

  boolean isEligibleForRetry(Exception e);

}
