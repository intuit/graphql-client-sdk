package com.intuit.graphql.sdk.client.resiliency;

import com.intuit.graphql.sdk.client.config.RetryConfiguration;
import com.intuit.graphql.sdk.client.exceptions.GraphQLSDKHttpException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetryRegister {

  @Getter
  private final Retry retry;
  private static final Logger LOG = LoggerFactory.getLogger(
      RetryRegister.class);

  public RetryRegister(RetryConfiguration retryConfiguration, String endpoint) {
    RetryConfig retryConfig = getRetryConfig(
        retryConfiguration);

    RetryRegistry retryRegistry = RetryRegistry.of(retryConfig);

    retryRegistry.getEventPublisher()
        .onEntryAdded(entryAddedEvent -> {
          Retry addedRetry = entryAddedEvent.getAddedEntry();
          LOG.info("Retry {} added", addedRetry.getName());

        })
        .onEntryRemoved(entryRemovedEvent -> {
          Retry removedRetry = entryRemovedEvent.getRemovedEntry();
          LOG.info("Retry {} removed", removedRetry.getName());

        });

    retry = retryRegistry.retry("GraphQLSDKRetry:" + endpoint);

    retry.getEventPublisher()
        .onSuccess(event -> LOG.info(event.toString()))
        .onError(event -> LOG.info(event.toString()))
        .onRetry(event -> LOG.info(event.toString()))
        .onIgnoredError(event -> LOG.info(event.toString()));
  }

  static RetryConfig getRetryConfig(
      RetryConfiguration retryConfiguration) {
    return RetryConfig.custom()
        .maxAttempts(retryConfiguration.getMaxAttempts())
        .intervalFunction(retryConfiguration.getIntervalFunction())
        .ignoreExceptions(CallNotPermittedException.class)
        .retryOnException(exception -> {
          if ((exception instanceof GraphQLSDKHttpException)) {
            return retryConfiguration.getRetryEligibilityStrategyForHttpStatusCodes()
                .isEligibleForRetry(
                    ((GraphQLSDKHttpException) exception).getStatusCode());
          } else {
            return retryConfiguration.getRetryEligibilityStrategyForExceptions()
                .isEligibleForRetry((Exception) exception);
          }
        })
        .build();
  }


}