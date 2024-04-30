package com.intuit.graphql.sdk.client.resiliency;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.intuit.graphql.sdk.client.resiliency.DefaultRetryEligibilityStrategy;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class DefaultRetryEligibilityStrategyTest {

  public static Object[][] testIsEligibleForRetryDataProvider() {
    return new Object[][]{
        {200, false},
        {201, false},
        {202, false},
        {203, false},
        {204, false},
        {205, false},
        {300, false},
        {301, false},
        {302, false},
        {303, false},
        {400, false},
        {401, false},
        {402, false},
        {403, false},
        {429, true},
        {500, true},
        {500, true},
        {503, true},
        {504, true}
    };
  }

  @ParameterizedTest
  @MethodSource("testIsEligibleForRetryDataProvider")
  void testIsEligibleForRetry(int statusCode, boolean expectedDecision) {
    DefaultRetryEligibilityStrategy defaultRetryEligibilityStrategy = new DefaultRetryEligibilityStrategy();
    assertEquals(defaultRetryEligibilityStrategy.isEligibleForRetry(statusCode),
        expectedDecision);
  }
}