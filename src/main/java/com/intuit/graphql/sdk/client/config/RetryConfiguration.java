package com.intuit.graphql.sdk.client.config;

import com.intuit.graphql.sdk.client.resiliency.DefaultRetryEligibilityStrategy;
import com.intuit.graphql.sdk.client.resiliency.RetryEligibilityStrategyForExceptions;
import com.intuit.graphql.sdk.client.resiliency.RetryEligibilityStrategyForHttpStatusCodes;
import io.github.resilience4j.core.IntervalFunction;
import lombok.Builder;
import lombok.Getter;


@Builder
@Getter
public class RetryConfiguration {

  @Builder.Default
  private int maxAttempts = 2;
  @Builder.Default
  private int retryIntervalMillis = 50;
  @Builder.Default
  private int exponentialBackOffMultiplier = 1;
  @Builder.Default
  private RetryEligibilityStrategyForHttpStatusCodes retryEligibilityStrategyForHttpStatusCodes = new DefaultRetryEligibilityStrategy();
  @Builder.Default
  private RetryEligibilityStrategyForExceptions retryEligibilityStrategyForExceptions = new DefaultRetryEligibilityStrategy();

  public IntervalFunction getIntervalFunction() {
    return IntervalFunction
        .ofExponentialBackoff(this.retryIntervalMillis,
            this.exponentialBackOffMultiplier);

  }
}
