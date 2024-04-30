package com.intuit.graphql.sdk.client.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.intuit.graphql.sdk.client.exceptions.GraphQLSDKHttpException;
import org.junit.jupiter.api.Test;

class GraphQLSDKHttpExceptionTest {

  @Test
  void test() {
    GraphQLSDKHttpException graphQLSDKHttpException = new GraphQLSDKHttpException(
        1, "Msg", "Response");
    assertEquals(1, graphQLSDKHttpException.getStatusCode());
    assertEquals("Msg", graphQLSDKHttpException.getMsg());
    assertEquals("Response", graphQLSDKHttpException.getResponse());
    assertEquals("Status code: 1, Message: Msg, Response: Response",
        graphQLSDKHttpException.getMessage());
  }

}