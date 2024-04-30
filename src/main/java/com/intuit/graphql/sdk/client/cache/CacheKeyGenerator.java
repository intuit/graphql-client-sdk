package com.intuit.graphql.sdk.client.cache;

import com.intuit.graphql.sdk.client.GraphQLRequest;

public interface CacheKeyGenerator {

    String generateCacheKey(GraphQLRequest graphQLRequest);

}
