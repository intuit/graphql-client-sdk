package com.intuit.graphql.sdk.client.resiliency;

public interface RetryEligibilityStrategyForHttpStatusCodes {

  boolean isEligibleForRetry(int statusCode);

}
