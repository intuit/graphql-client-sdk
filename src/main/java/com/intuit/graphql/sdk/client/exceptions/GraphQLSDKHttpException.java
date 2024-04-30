package com.intuit.graphql.sdk.client.exceptions;

import lombok.Getter;

@Getter
public class GraphQLSDKHttpException extends RuntimeException {

  private final int statusCode;
  private final String msg;
  private final String response;

  public GraphQLSDKHttpException(int statusCode,
      String msg, String response) {
    super(
        String.format("Status code: %d, Message: %s, Response: %s", statusCode,
            msg, response));
    this.statusCode = statusCode;
    this.msg = msg;
    this.response = response;
  }
}
