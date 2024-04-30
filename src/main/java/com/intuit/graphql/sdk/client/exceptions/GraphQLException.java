package com.intuit.graphql.sdk.client.exceptions;

public class GraphQLException extends
    Exception {

  public GraphQLException(String message) {
    super(message);
  }

  public GraphQLException(Exception e) {
    super(e);
  }
}
