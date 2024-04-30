package com.intuit.graphql.sdk.client.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import com.intuit.graphql.sdk.client.config.HttpConfiguration;
import com.intuit.graphql.sdk.client.exceptions.GraphQLRuntimeException;
import com.intuit.graphql.sdk.client.exceptions.GraphQLSDKHttpException;
import com.intuit.graphql.sdk.client.http.GraphQLHTTPClient;
import com.intuit.graphql.sdk.client.http.GraphQLHttpClientBuilder;
import com.netflix.graphql.dgs.client.HttpResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import org.apache.http.Header;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class GraphQLHttpClientBuilderTest {
  private static final String SET_COOKIE_KEY = "Set-Cookie";
  private static final String COOKIE_VALUE_1 = "abc";
  private static final String COOKIE_VALUE_2 = "def";


  @Mock
  private CloseableHttpClient closeableHttpClient;


  @Test
  void testBuilder() {
    GraphQLHTTPClient client = new GraphQLHttpClientBuilder(
        HttpConfiguration.builder().build()).build();
    assertNotNull(client);
  }

  @Test
  void testGetPostRequest() throws IOException {
    HttpPost postRequest = new GraphQLHttpClientBuilder(
        HttpConfiguration.builder().build()).getPostRequest(
        "http://example.org/graphql",
        Collections.emptyMap(), "body-123");
    assertEquals("http://example.org/graphql", postRequest.getURI().toString());
    assertEquals("body-123", EntityUtils.toString(postRequest.getEntity()));
    assertEquals(0, postRequest.getAllHeaders().length);
  }

  @Test
  void testGetRequestConfig() {
    RequestConfig requestConfig = new GraphQLHttpClientBuilder(
        HttpConfiguration.builder().connectTimeoutMilliSeconds(123)
            .connectionRequestTimeoutMilliSeconds(456)
            .socketTimeOutMilliSeconds(789)
            .build()).getRequestConfigWithTimeOutsConfigured();
    assertEquals(123, requestConfig.getConnectTimeout());
    assertEquals(456, requestConfig.getConnectionRequestTimeout());
    assertEquals(789, requestConfig.getSocketTimeout());
  }

  @Test
  void testGetConnectionPoolManager() {
    PoolingHttpClientConnectionManager connectionPoolManager = new GraphQLHttpClientBuilder(
        HttpConfiguration.builder().maxTotalConnections(123)
            .maxConnectionsPerRoute(456)
            .build()).getConnectionPoolManager(
    );
    assertEquals(123, connectionPoolManager.getMaxTotal());
    assertEquals(456, connectionPoolManager.getDefaultMaxPerRoute());
  }

  @Test
  void testExecutionOfTheRequestForSuccessResponse() throws IOException {
    GraphQLHTTPClient testableGraphQLClient = getTestableGraphQLClient();
    CloseableHttpResponse mockResponse = getMockResponse(200);
    ArgumentCaptor<HttpPost> argumentCaptor = ArgumentCaptor.forClass(
        HttpPost.class);
    when(closeableHttpClient.execute(argumentCaptor.capture()))
        .thenReturn(mockResponse);

    HttpResponse response = testableGraphQLClient.execute(
        "http://example.org/graphql", Collections.emptyMap(), "body-123");

    assertEquals("http://example.org/graphql",
        argumentCaptor.getValue().getURI().toString());
    assertEquals("body-123",
        EntityUtils.toString(argumentCaptor.getValue().getEntity()));
    assertEquals(0, argumentCaptor.getValue().getAllHeaders().length);
    assertEquals(200, response.getStatusCode());
  }

  @ParameterizedTest
  @MethodSource("testExecutionOfTheRequestForFailedResponseDataProvider")
  void testExecutionOfTheRequestForFailedResponse(int statusCode)
      throws IOException {
    GraphQLHTTPClient testableGraphQLClient = getTestableGraphQLClient();
    CloseableHttpResponse mockResponse = getMockResponse(statusCode);
    ArgumentCaptor<HttpPost> argumentCaptor = ArgumentCaptor.forClass(
        HttpPost.class);
    when(closeableHttpClient.execute(argumentCaptor.capture()))
        .thenReturn(mockResponse);

    Map<String, List<String>> map = Collections.emptyMap();

    Assertions.assertThrows(GraphQLSDKHttpException.class,
        () -> testableGraphQLClient.execute("http://example.org/graphql", map,
            "body-123"));
  }

  @Test
  void testExecutionOfTheRequestForSocketTimeoutException() throws IOException {
    GraphQLHTTPClient testableGraphQLClient = getTestableGraphQLClient();
    ArgumentCaptor<HttpPost> argumentCaptor = ArgumentCaptor.forClass(
        HttpPost.class);
    when(closeableHttpClient.execute(argumentCaptor.capture()))
        .thenThrow(new SocketTimeoutException("Socket timed out"));

    Map<String, List<String>> map = Collections.emptyMap();

    Assertions.assertThrows(GraphQLRuntimeException.class,
        () -> testableGraphQLClient.execute("http://example.org/graphql", map,
            "body-123"));
  }

  @Test
  void testReturnOfHttpResponseHeadersWithValidHeaders() throws IOException {
    List<String> cookies = getMockCookies();
    GraphQLHTTPClient testableGraphQLClient = getTestableGraphQLClient();
    CloseableHttpResponse mockResponse = getMockResponseWithCookiesInHeaders(200, cookies);
    when(closeableHttpClient.execute(Mockito.any()))
            .thenReturn(mockResponse);

    HttpResponse response = testableGraphQLClient.execute(
            "http://example.org/graphql", Collections.emptyMap(), "body-123");

    Assertions.assertEquals(cookies, response.getHeaders().get(SET_COOKIE_KEY));
  }

  @Test
  void testReturnOfHttpResponseHeadersWithEmptyHeaders() throws IOException {
    GraphQLHTTPClient testableGraphQLClient = getTestableGraphQLClient();
    CloseableHttpResponse mockResponse = getMockResponse(200);
    when(closeableHttpClient.execute(Mockito.any()))
            .thenReturn(mockResponse);

    HttpResponse response = testableGraphQLClient.execute(
            "http://example.org/graphql", Collections.emptyMap(), "body-123");

    Map<String, List<String>> responseHeaders = response.getHeaders();
    Assertions.assertNotNull(responseHeaders);
    Assertions.assertEquals(0, responseHeaders.size());
  }

  public static List<Integer> testExecutionOfTheRequestForFailedResponseDataProvider() {
    return Arrays.asList(400, 401, 403, 404, 429, 500, 502, 503, 504);
  }

  private GraphQLHTTPClient getTestableGraphQLClient() {
    return new GraphQLHttpClientBuilder(
        HttpConfiguration.builder().build()) {
      @Override
      CloseableHttpClient getCloseableHttpClient(
          PoolingHttpClientConnectionManager connectionManager,
          RequestConfig requestConfig) {
        return closeableHttpClient;
      }
    }.build();
  }

  @NotNull
  private static CloseableHttpResponse getMockResponse(final int statusCode)
      throws UnsupportedEncodingException {
    CloseableHttpResponse mockResponse = Mockito.mock(
        CloseableHttpResponse.class);
    when(mockResponse.getStatusLine()).thenReturn(new StatusLine() {
      @Override
      public ProtocolVersion getProtocolVersion() {
        return new ProtocolVersion("http", 2, 1);
      }

      @Override
      public int getStatusCode() {
        return statusCode;
      }

      @Override
      public String getReasonPhrase() {
        return "all good from test";
      }
    });
    when(mockResponse.getEntity()).thenReturn(
        new StringEntity("test response body"));
    return mockResponse;
  }

  private static CloseableHttpResponse getMockResponseWithCookiesInHeaders(final int statusCode, List<String> cookies)
          throws UnsupportedEncodingException {
    CloseableHttpResponse mockResponse = getMockResponse(statusCode);

    List<Header> cookiesHeader = new ArrayList<>();
    for (String cookie : cookies) {
      cookiesHeader.add(new BasicHeader(SET_COOKIE_KEY, cookie));
    }

    when(mockResponse.getAllHeaders()).thenReturn(cookiesHeader.stream().toArray(Header[] ::new));
    return mockResponse;
  }

  private static List<String> getMockCookies() {
    List<String> cookies = new ArrayList<>();
    cookies.add(COOKIE_VALUE_1);
    cookies.add(COOKIE_VALUE_2);
    return cookies;
  };
}
