package com.intuit.graphql.sdk.client.exceptions;

import com.netflix.graphql.dgs.client.GraphQLResponse;
import lombok.Getter;

@Getter
public class GraphQLErrorResponseException extends
    GraphQLException {

  private final GraphQLResponse graphQLResponse;

  public GraphQLErrorResponseException(
      GraphQLResponse graphQLResponse) {
    super("GraphQL response has errors");
    this.graphQLResponse = graphQLResponse;
  }
}
