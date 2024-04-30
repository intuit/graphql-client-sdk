package com.intuit.graphql.sdk.client.http;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.intuit.graphql.sdk.client.http.ConnectionKeepAliveStrategy;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConnectionKeepAliveStrategyTest {

  HttpContext context;
  HttpResponse response;

  private ConnectionKeepAliveStrategy connectionKeepAliveStrategy;

  @BeforeEach
  public void setUp() {
    context = new BasicHttpContext(null);
    response = new BasicHttpResponse(
        new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK"));
  }

  @Test
  void noConnectionTimeoutHeaderProvided() {
    connectionKeepAliveStrategy = new ConnectionKeepAliveStrategy();
    assertEquals(5 * 60 * 1000,
        connectionKeepAliveStrategy.getKeepAliveDuration(response, context));
  }

  @Test
  void noConnectionTimeoutHeaderProvidedAndDefaultTimeoutInStrategy() {
    connectionKeepAliveStrategy = new ConnectionKeepAliveStrategy(4);
    assertEquals(4 * 1000,
        connectionKeepAliveStrategy.getKeepAliveDuration(response, context));
  }

  @Test
  void validConnectionTimeoutHeaderProvided() {
    this.response.addHeader("Keep-Alive", "timeout=3, max=20");
    connectionKeepAliveStrategy = new ConnectionKeepAliveStrategy();
    assertEquals(3 * 1000,
        connectionKeepAliveStrategy.getKeepAliveDuration(response, context));
  }

  @Test
  void invalidConnectionTimeouteHeaderProvided() {
    this.response.addHeader("Keep-Alive", "timeout=-1, max=20");
    connectionKeepAliveStrategy = new ConnectionKeepAliveStrategy(4);
    assertEquals(4 * 1000,
        connectionKeepAliveStrategy.getKeepAliveDuration(response, context));
  }
}