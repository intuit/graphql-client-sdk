package com.intuit.graphql.sdk.client.cache;

import com.netflix.graphql.dgs.client.GraphQLResponse;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class SerializableGraphQLResponse implements Serializable {
  private static final long serialVersionUID = -6830290176763507998L;
  private final String json;
  private final Map<String, List<String>> headers;
  private transient GraphQLResponse graphQLResponse;

  public SerializableGraphQLResponse(GraphQLResponse graphQLResponse) {
    this.json = graphQLResponse.getJson();
    this.headers = graphQLResponse.getHeaders();
    this.graphQLResponse = graphQLResponse;
  }

  public GraphQLResponse getGraphQLResponse() {
    if (graphQLResponse == null) {
      graphQLResponse = new GraphQLResponse(json, headers);
    }
    return graphQLResponse;
  }
}
