package com.intuit.graphql.sdk.client.config;


import static org.junit.jupiter.api.Assertions.assertEquals;

import com.intuit.graphql.sdk.client.config.HttpConfiguration;
import org.junit.jupiter.api.Test;

class HttpConfigurationTest {

  @Test
  void testDefaultValuesAreSet() {
    HttpConfiguration configuration = HttpConfiguration.builder().build();
    assertEquals(10, configuration.getMaxTotalConnections());
    assertEquals(10, configuration.getMaxConnectionsPerRoute());
    assertEquals(1000, configuration.getConnectTimeoutMilliSeconds());
    assertEquals(1000, configuration.getConnectionRequestTimeoutMilliSeconds());
    assertEquals(1000, configuration.getSocketTimeOutMilliSeconds());
    assertEquals(60, configuration.getConnectionKeepLiveTimeSeconds());
    assertEquals(30, configuration.getMaxConnectionIdleTimeSeconds());
  }

  @Test
  void testValueSettingViaBuilder() {
    HttpConfiguration configuration = HttpConfiguration.builder()
        .connectTimeoutMilliSeconds(123)
        .connectionRequestTimeoutMilliSeconds(123)
        .maxConnectionIdleTimeSeconds(123)
        .maxConnectionsPerRoute(123)
        .maxTotalConnections(123)
        .socketTimeOutMilliSeconds(123)
        .connectionKeepLiveTimeSeconds(123).build();
    assertEquals(123, configuration.getMaxTotalConnections());
    assertEquals(123, configuration.getMaxConnectionsPerRoute());
    assertEquals(123, configuration.getConnectTimeoutMilliSeconds());
    assertEquals(123, configuration.getConnectionRequestTimeoutMilliSeconds());
    assertEquals(123, configuration.getSocketTimeOutMilliSeconds());
    assertEquals(123, configuration.getConnectionKeepLiveTimeSeconds());
    assertEquals(123, configuration.getMaxConnectionIdleTimeSeconds());
  }

}