package com.intuit.graphql.sdk.client.http;

import com.intuit.graphql.sdk.client.config.HttpConfiguration;
import com.intuit.graphql.sdk.client.exceptions.GraphQLRuntimeException;
import com.intuit.graphql.sdk.client.exceptions.GraphQLSDKHttpException;
import com.netflix.graphql.dgs.client.HttpResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

public class GraphQLHttpClientBuilder {

  private final HttpConfiguration config;

  public GraphQLHttpClientBuilder(HttpConfiguration configuration) {
    this.config = configuration;
  }

  public GraphQLHTTPClient build() {
    PoolingHttpClientConnectionManager connectionManager = getConnectionPoolManager();
    RequestConfig requestConfig = getRequestConfigWithTimeOutsConfigured();
    CloseableHttpClient httpClient = getCloseableHttpClient(connectionManager,
        requestConfig);

    return (url, headers, body) -> {
      try {
        CloseableHttpResponse httpResponse = httpClient.execute(
            getPostRequest(url, headers, body));
        if (httpResponse.getStatusLine().getStatusCode() > 299
            || httpResponse.getStatusLine().getStatusCode() < 200) {
          throw new GraphQLSDKHttpException(
              httpResponse.getStatusLine().getStatusCode(),
              httpResponse.getStatusLine().getReasonPhrase(),
              EntityUtils.toString(httpResponse.getEntity(),
                  StandardCharsets.UTF_8));
        }

        Map<String, List<String>> responseHeaders = extractHeadersFromResponse(httpResponse.getAllHeaders());
        return new HttpResponse(httpResponse.getStatusLine().getStatusCode(),
            EntityUtils.toString(httpResponse.getEntity(),
                StandardCharsets.UTF_8), responseHeaders);
      } catch (IOException e) {
        throw new GraphQLRuntimeException(e);
      }
    };
  }

  HttpPost getPostRequest(String url,
      Map<String, ? extends List<String>> headers, String body) {
    HttpPost httpPost = new HttpPost(url);
    headers.forEach(
        (key, value) -> httpPost.setHeader(key, String.join(",", value)));
    httpPost.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
    return httpPost;
  }

  CloseableHttpClient getCloseableHttpClient(
      PoolingHttpClientConnectionManager connectionManager,
      RequestConfig requestConfig) {
    return HttpClientBuilder.create()
        .setConnectionManager(connectionManager)
        .setDefaultRequestConfig(requestConfig)
        .setKeepAliveStrategy(new ConnectionKeepAliveStrategy(
            config.getConnectionKeepLiveTimeSeconds()))
        .disableAutomaticRetries()
        .evictExpiredConnections()
        .evictIdleConnections(config.getMaxConnectionIdleTimeSeconds(),
            TimeUnit.SECONDS)
        .build();
  }

  RequestConfig getRequestConfigWithTimeOutsConfigured() {
    RequestConfig.Builder custom = RequestConfig.custom();
    custom.setConnectTimeout(config.getConnectTimeoutMilliSeconds());
    custom.setConnectionRequestTimeout(
        config.getConnectionRequestTimeoutMilliSeconds());
    custom.setSocketTimeout(config.getSocketTimeOutMilliSeconds());
    return custom.build();
  }

  PoolingHttpClientConnectionManager getConnectionPoolManager() {
    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    connectionManager.setMaxTotal(config.getMaxTotalConnections());
    connectionManager.setDefaultMaxPerRoute(config.getMaxConnectionsPerRoute());
    connectionManager.closeIdleConnections(
        config.getMaxConnectionIdleTimeSeconds(), TimeUnit.SECONDS);
    return connectionManager;
  }

  private static Map<String, List<String>> extractHeadersFromResponse(Header[] httpResponseHeaders) {
    Map<String, List<String>> result = new HashMap<>();
    if (httpResponseHeaders == null) {
      return result;
    }
    Arrays.stream(httpResponseHeaders)
            .forEach(header -> addHeaderToMap(header, result));
    return result;
  }

  private static void addHeaderToMap(Header header, Map<String, List<String>> result) {
    List<String> headerValues = Optional
            .ofNullable(result.get(header.getName()))
            .orElseGet(ArrayList::new);
    headerValues.add(header.getValue());
    result.put(header.getName(), headerValues);
  }
}
