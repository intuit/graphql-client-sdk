package com.intuit.graphql.sdk.client.resiliency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.intuit.graphql.sdk.client.config.RetryConfiguration;
import com.intuit.graphql.sdk.client.exceptions.GraphQLRuntimeException;
import com.intuit.graphql.sdk.client.exceptions.GraphQLSDKHttpException;
import com.intuit.graphql.sdk.client.resiliency.RetryRegister;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import java.net.SocketTimeoutException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RetryRegisterTest {


  @Test
  void testRegisterRetry() {
    Retry retry = new RetryRegister(RetryConfiguration.builder().build(),
        "test").getRetry();
    assertNotNull(retry);
    assertEquals("GraphQLSDKRetry:test", retry.getName());
    RetryConfig retryConfig = retry.getRetryConfig();
    assertEquals(2, retryConfig.getMaxAttempts());
    assertNotNull(retryConfig.getIntervalFunction());
  }

  @Test
  void testGetRetryConfig() {
    RetryConfig retryConfig = RetryRegister.getRetryConfig(
        RetryConfiguration.builder().build());
    assertNotNull(retryConfig);
    assertEquals(2, retryConfig.getMaxAttempts());
    assertEquals(50, retryConfig.getIntervalFunction().apply(2));
    assertTrue(
        retryConfig.getExceptionPredicate().test(new SocketTimeoutException()));
    assertTrue(retryConfig.getExceptionPredicate().test(
        new GraphQLRuntimeException(new SocketTimeoutException())));
    assertFalse(
        retryConfig.getExceptionPredicate().test(new RuntimeException()));
    assertTrue(retryConfig.getExceptionPredicate().test(
        new GraphQLSDKHttpException(429, "Too Many Requests", "")));
    assertFalse(retryConfig.getExceptionPredicate().test(
        new GraphQLSDKHttpException(401, "Too Many Requests", "")));
  }

}