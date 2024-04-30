package com.intuit.graphql.sdk.client.cache;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
public class CacheBehavior {

  // Optional context to send to your cache implementation
  private final Map<String, String> cacheContext;
  @Builder.Default
  private final boolean isCacheEnabled = true;
  @Builder.Default
  private final int ttl = 5;
  @Builder.Default
  private final TimeUnit timeUnit = TimeUnit.MINUTES;

}
