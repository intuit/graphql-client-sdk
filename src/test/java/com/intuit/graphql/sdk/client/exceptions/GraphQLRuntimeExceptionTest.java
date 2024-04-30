package com.intuit.graphql.sdk.client.exceptions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.intuit.graphql.sdk.client.exceptions.GraphQLRuntimeException;
import org.junit.jupiter.api.Test;

class GraphQLRuntimeExceptionTest {

  @Test
  void test() {
    RuntimeException cause = new RuntimeException();
    GraphQLRuntimeException graphQLRuntimeException = new GraphQLRuntimeException(
        cause);
    assertNotNull(graphQLRuntimeException);
    assertEquals(cause, graphQLRuntimeException.getCause());
  }

}