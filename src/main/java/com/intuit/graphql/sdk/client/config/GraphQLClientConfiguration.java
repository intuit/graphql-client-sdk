package com.intuit.graphql.sdk.client.config;


import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder
public class GraphQLClientConfiguration {

  @NonNull
  private HttpConfiguration httpConfiguration;

  @NonNull
  private String graphQLApiUrl;

  @NonNull
  private ResiliencyConfiguration resilienceConfiguration;

  private CacheConfiguration cacheConfiguration;
}
