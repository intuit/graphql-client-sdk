package com.intuit.graphql.sdk.client.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.intuit.graphql.sdk.client.GraphQLRequest;
import com.intuit.graphql.sdk.client.cache.CacheKeyGenerator;
import com.intuit.graphql.sdk.client.cache.DefaultCacheKeyGenerator;
import com.intuit.graphql.sdk.client.cache.GraphQLCache;
import com.intuit.graphql.sdk.client.cache.SerializableGraphQLResponse;
import com.intuit.graphql.sdk.client.config.CacheConfiguration;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CacheConfigurationTest {

  public static Stream<Arguments> testMandatoryConfigurationsDataProvider() {
    return Stream.of(
        Arguments.of(null, null),
        Arguments.of(null, new TestCache()),
        Arguments.of(new DefaultCacheKeyGenerator("abc", Collections.emptyList()), null)
    );
  }

  @ParameterizedTest
  @MethodSource("testMandatoryConfigurationsDataProvider")
  void testMandatoryConfigurations(CacheKeyGenerator cacheKeyGenerator,
      GraphQLCache graphQLCache) {
    CacheConfiguration.CacheConfigurationBuilder builder = CacheConfiguration.builder();
    assertThrows(NullPointerException.class, () -> builder.cacheKeyGenerator(cacheKeyGenerator).graphQLCache(graphQLCache).build());
  }

  @Test
  void testConfiguration() {
    CacheConfiguration.CacheConfigurationBuilder builder = CacheConfiguration.builder();
    CacheConfiguration cacheConfiguration = builder.cacheKeyGenerator(new DefaultCacheKeyGenerator("abc", Collections.emptyList())).graphQLCache(new TestCache()).build();
    assertNotNull(cacheConfiguration);
    assertNotNull(cacheConfiguration.getGraphQLCache());
    assertNotNull(cacheConfiguration.getCacheKeyGenerator());
  }

  private static class TestCache implements GraphQLCache {

    @Override
    public void set(GraphQLRequest graphQLRequest, String key, SerializableGraphQLResponse value, int ttl,
                    TimeUnit timeUnit) {

    }

    @Override
    public Optional<SerializableGraphQLResponse> get(GraphQLRequest graphQLRequest, String key) {
      return Optional.empty();
    }
  }
}