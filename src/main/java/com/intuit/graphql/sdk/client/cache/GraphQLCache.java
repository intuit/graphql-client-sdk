package com.intuit.graphql.sdk.client.cache;


import com.intuit.graphql.sdk.client.GraphQLRequest;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public interface GraphQLCache {

  void set(GraphQLRequest request, String key, SerializableGraphQLResponse value, int ttl, TimeUnit timeUnit);

  Optional<SerializableGraphQLResponse> get(GraphQLRequest request, String key);

}
