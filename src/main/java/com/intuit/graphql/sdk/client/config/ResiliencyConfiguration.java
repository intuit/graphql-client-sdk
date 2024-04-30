package com.intuit.graphql.sdk.client.config;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
@Getter
public class ResiliencyConfiguration {

  @Builder.Default
  boolean isEnabled = true;
  @NonNull
  private RetryConfiguration retryConfig;
  @NonNull
  private CircuitBreakerConfiguration circuitBreakerConfig;
}
