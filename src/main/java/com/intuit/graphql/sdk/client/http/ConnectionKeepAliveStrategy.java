package com.intuit.graphql.sdk.client.http;

import org.apache.http.HttpResponse;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.protocol.HttpContext;

/**
 * This class decides how long a connection can remain idle before being reused.
 */
public class ConnectionKeepAliveStrategy extends
    DefaultConnectionKeepAliveStrategy {

  private final long defaultKeepAliveMillis;

  public ConnectionKeepAliveStrategy() {
    // 5min in seconds
    this(5 * 60);
  }

  public ConnectionKeepAliveStrategy(long defaultKeepAliveSeconds) {
    this.defaultKeepAliveMillis = defaultKeepAliveSeconds * 1000L;
  }

  // Must be thread-safe
  @Override
  public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
    // The default strategy uses the timeout value in the Keep-Alive header
    // Keep-Alive: timeout=15, max=100
    long keepAliveMilliSeconds = super.getKeepAliveDuration(response, context);
    if (keepAliveMilliSeconds <= 0) {
      /*
       * If the Keep-Alive header is not present in the response,
       * HttpClient assumes the connection can be kept alive indefinitely.
       * The server will; however, very likely close idle connections
       * after a short period of time. If we don't close the connections
       * on the client side, we end up with a bunch of stale connections
       * in the pool. So set icleart to a realistic value to reduce the number
       * of stale connections we have in the pool.
       *
       */
      keepAliveMilliSeconds = defaultKeepAliveMillis;
    }

    return keepAliveMilliSeconds;
  }
}
