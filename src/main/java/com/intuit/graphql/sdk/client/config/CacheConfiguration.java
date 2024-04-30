package com.intuit.graphql.sdk.client.config;

import com.intuit.graphql.sdk.client.cache.CacheKeyGenerator;
import com.intuit.graphql.sdk.client.cache.GraphQLCache;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder
public class CacheConfiguration {

  @NonNull
  private final GraphQLCache graphQLCache;

  @NonNull
  private final CacheKeyGenerator cacheKeyGenerator;

}
