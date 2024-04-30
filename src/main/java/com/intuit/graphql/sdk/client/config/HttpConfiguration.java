package com.intuit.graphql.sdk.client.config;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HttpConfiguration {

  @Builder.Default
  private int maxTotalConnections = 10;
  @Builder.Default
  private int maxConnectionsPerRoute = 10;
  @Builder.Default
  private int connectTimeoutMilliSeconds = 1000;
  @Builder.Default
  private int connectionRequestTimeoutMilliSeconds = 1000;
  @Builder.Default
  private int socketTimeOutMilliSeconds = 1000;
  @Builder.Default
  private int connectionKeepLiveTimeSeconds = 60;
  @Builder.Default
  private int maxConnectionIdleTimeSeconds = 30;

}

