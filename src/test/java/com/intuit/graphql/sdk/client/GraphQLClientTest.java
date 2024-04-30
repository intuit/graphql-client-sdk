package com.intuit.graphql.sdk.client;


import static com.intuit.graphql.sdk.client.util.Pair.build;
import static com.intuit.graphql.sdk.client.util.Pair.buildMapFor;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.intuit.graphql.sdk.TestUtil;
import com.intuit.graphql.sdk.client.cache.DefaultCacheKeyGenerator;
import com.intuit.graphql.sdk.client.cache.GraphQLCache;
import com.intuit.graphql.sdk.client.cache.SerializableGraphQLResponse;
import com.intuit.graphql.sdk.client.config.CacheConfiguration;
import com.intuit.graphql.sdk.client.config.CircuitBreakerConfiguration;
import com.intuit.graphql.sdk.client.config.HttpConfiguration;
import com.intuit.graphql.sdk.client.config.GraphQLClientConfiguration;
import com.intuit.graphql.sdk.client.config.ResiliencyConfiguration;
import com.intuit.graphql.sdk.client.config.RetryConfiguration;
import com.intuit.graphql.sdk.client.exceptions.GraphQLException;
import com.intuit.graphql.sdk.client.http.GraphQLHTTPClient;
import com.intuit.graphql.sdk.client.util.LogEvent;
import com.intuit.graphql.sdk.client.util.Pair;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.netflix.graphql.dgs.client.HttpResponse;
import graphql.Assert;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraphQLClientTest {

    private GraphQLClient client;
    @Mock
    private GraphQLHTTPClient httpClient;
    @Mock
    private GraphQLClientConfiguration configuration;
    @Mock
    private ResiliencyConfiguration resiliencyConfiguration;
    @Mock
    private RequestHandler mockRequestHandler;


    @Test
    void testExecuteValidRequestReturnsResponse()
        throws GraphQLException {
        when(configuration.getGraphQLApiUrl()).thenReturn(
            "http://localhost:8080/graphql");
        when(configuration.getResilienceConfiguration()).thenReturn(
            resiliencyConfiguration);
        when(resiliencyConfiguration.isEnabled()).thenReturn(false);
        client = new GraphQLClient(configuration, httpClient);
        Mockito.when(httpClient.execute(Mockito.anyString(), Mockito.anyMap(),
                Mockito.anyString()))
            .thenReturn(
                new HttpResponse(200, "{\"data\": {\"test\": \"test\"}}"));
        GraphQLRequest request = GraphQLRequest.builder()
                .tid("tid")
                .authentication(() -> "authHeaderForTesting")
                .query("query-curious")
                .operation("operation-delta")
                .variables(Collections.emptyMap())
                .httpHeaders(getTestHeaders())
                .build();
        GraphQLResponse actualResponse = client.execute(request);
        Assert.assertNotNull(actualResponse);
        assertEquals("test", actualResponse.extractValue("data.test"));

    }

    @Test
    void testExecuteValidRequestReturnsResponseWithResiliencyHandler()
            throws GraphQLException {
        CircuitBreakerConfiguration circuitBreakerConfiguration = CircuitBreakerConfiguration.builder()
                .build();
        RetryConfiguration retryConfiguration = RetryConfiguration.builder()
                .build();
        when(configuration.getGraphQLApiUrl()).thenReturn(
                "http://localhost:8080/graphql");
        when(configuration.getResilienceConfiguration()).thenReturn(
                resiliencyConfiguration);
        when(resiliencyConfiguration.isEnabled()).thenReturn(true);
        when(resiliencyConfiguration.getCircuitBreakerConfig()).thenReturn(
                circuitBreakerConfiguration);
        when(resiliencyConfiguration.getRetryConfig()).thenReturn(
                retryConfiguration);
        client = new GraphQLClient(configuration, httpClient);
        Mockito.when(httpClient.execute(Mockito.anyString(), Mockito.anyMap(),
                        Mockito.anyString()))
                .thenReturn(new HttpResponse(200, "{\"data\": {\"test\": \"test\"}}"));
        GraphQLRequest request = GraphQLRequest.builder()
                .tid("tid")
                .authentication(() -> "authHeaderForTesting")
                .query("query-curious")
                .operation("operation-delta")
                .variables(Collections.emptyMap())
                .httpHeaders(getTestHeaders())
                .build();
        GraphQLResponse actualResponse = client.execute(request);
        Assert.assertNotNull(actualResponse);
        assertEquals("test", actualResponse.extractValue("data.test"));
    }

    @Test
    void testGraphQLClientExternalFacingConstructor() {
        GraphQLClient client = new GraphQLClient(
                GraphQLClientConfiguration
                        .builder()
                        .graphQLApiUrl("http://localhost:8080/graphql")
                        .resilienceConfiguration(ResiliencyConfiguration.builder()
                                .retryConfig(RetryConfiguration.builder().build())
                                .circuitBreakerConfig(
                                        CircuitBreakerConfiguration.builder().build())
                                .build())
                        .httpConfiguration(HttpConfiguration.builder().build())
                        .build());
        Assertions.assertNotNull(client);
    }

    @Test
    void testGraphQLClientExternalFacingConstructorWithCache() {
        GraphQLClient client = new GraphQLClient(
                GraphQLClientConfiguration
                        .builder()
                        .graphQLApiUrl("http://localhost:8080/graphql")
                        .resilienceConfiguration(ResiliencyConfiguration.builder()
                                .retryConfig(RetryConfiguration.builder().build())
                                .circuitBreakerConfig(
                                        CircuitBreakerConfiguration.builder().build())
                                .build())
                        .httpConfiguration(HttpConfiguration.builder().build())
                        .cacheConfiguration(CacheConfiguration.builder().graphQLCache(
                                        new GraphQLCache() {
                                            @Override
                                            public void set(GraphQLRequest request, String key, SerializableGraphQLResponse value,
                                                            int ttl, TimeUnit timeUnit) {

                                            }

                                            @Override
                                            public Optional<SerializableGraphQLResponse> get(GraphQLRequest request, String key) {
                                                return Optional.empty();
                                            }
                                        })
                                .cacheKeyGenerator(new DefaultCacheKeyGenerator("http://localhost:8080/graphql", Collections.emptyList())).build())
                        .build());
        Assertions.assertNotNull(client);
    }

    @Test
    void testAddDetailsInLogEventWithAuthenticationAndNoAuthorizationHeader() {
        LogEvent logEvent = new LogEvent();
        when(configuration.getGraphQLApiUrl()).thenReturn(
            "http://localhost:8080/graphql");
        when(configuration.getResilienceConfiguration()).thenReturn(
            resiliencyConfiguration);
        when(resiliencyConfiguration.isEnabled()).thenReturn(false);
        client = new GraphQLClient(configuration, httpClient);
        GraphQLRequest request = GraphQLRequest.builder()
            .logEvent(logEvent)
            .tid("tid")
            .authentication(() -> "authentication")
            .query("query-curious")
            .operation("operation-delta")
            .variables(Collections.emptyMap())
            .httpHeaders(getTestHeaders())
                .build();
        client.addDetailsInLogEvent(request);
        assertEquals("url=http://localhost:8080/graphql, tid=tid, query=query-curious, variables={}, operation=operation-delta", logEvent.getLogEntry());
    }

    @Test
    void testAddDetailsInLogEventWithAuthenticationAndAuthorizationHeader() {
        LogEvent logEvent = new LogEvent();
        HashMap<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "ShouldNotBeUsedOrLogged");
        when(configuration.getGraphQLApiUrl()).thenReturn(
            "http://localhost:8080/graphql");
        when(configuration.getResilienceConfiguration()).thenReturn(
            resiliencyConfiguration);
        when(resiliencyConfiguration.isEnabled()).thenReturn(false);
        client = new GraphQLClient(configuration, httpClient);
        GraphQLRequest request = GraphQLRequest.builder()
            .logEvent(logEvent)
            .tid("tid")
            .authentication(() -> "authentication")
            .query("query-curious")
            .operation("operation-delta")
            .variables(Collections.emptyMap())
            .httpHeaders(getTestHeaders())
                .build();
        client.addDetailsInLogEvent(request);
        assertEquals("url=http://localhost:8080/graphql, tid=tid, query=query-curious, variables={}, operation=operation-delta", logEvent.getLogEntry());
    }

    @Test
    void testAddDetailsInLogEventWithAuthenticationAndNullHeaders() {
        LogEvent logEvent = new LogEvent();
        HashMap<String, String> headersMap = new HashMap<>();
        headersMap.put("Authorization", "ShouldNotBeUsedOrLogged");
        when(configuration.getGraphQLApiUrl()).thenReturn(
            "http://localhost:8080/graphql");
        when(configuration.getResilienceConfiguration()).thenReturn(
            resiliencyConfiguration);
        when(resiliencyConfiguration.isEnabled()).thenReturn(false);
        client = new GraphQLClient(configuration, httpClient);
        GraphQLRequest request = GraphQLRequest.builder()
            .logEvent(logEvent)
            .tid("tid")
            .authentication(() -> "authentication")
            .query("query-curious")
            .operation("operation-delta")
            .variables(Collections.emptyMap())
            .httpHeaders(null)
                .build();
        client.addDetailsInLogEvent(request);
        assertEquals("url=http://localhost:8080/graphql, tid=tid, query=query-curious, variables={}, operation=operation-delta", logEvent.getLogEntry());
    }

    @Test
    void testExecuteValidPaginatedRequestReturnsPaginatedResponse()
            throws Exception {
        when(configuration.getGraphQLApiUrl()).thenReturn(
                "http://localhost:8080/graphql");
        when(configuration.getResilienceConfiguration()).thenReturn(
                resiliencyConfiguration);
        when(resiliencyConfiguration.isEnabled()).thenReturn(false);
        client = new GraphQLClient(configuration, httpClient);
        Mockito.when(httpClient.execute(Mockito.anyString(), Mockito.anyMap(),
                        Mockito.anyString()))
                .thenReturn(new HttpResponse(200, TestUtil.readResourceFile("sample-paginated-response.json")));
        PaginatedGraphQLRequest request = PaginatedGraphQLRequest.builder()
                .tid("tid")
                .authentication(() -> "authHeaderForTesting")
                .query("query-curious")
                .operation("operation-delta")
                .variables(new HashMap<>())
                .httpHeaders(getTestHeaders())
                .pageSizeFieldName("first")
                .pageSize(10)
                .pageInfoJsonPath(PaginatedGraphQLRequestTest.PAGE_INFO_JSON_PATH)
                .endCursorFieldName("after")
                .build();
        PaginatedGraphQLResponse paginatedResponse = client.execute(request);
        Assert.assertNotNull(paginatedResponse);
        assertEquals("test", paginatedResponse.getResponse().extractValue("data.paginatedQuery.node"));
        assertFalse(paginatedResponse.hasNextPage());
        assertTrue(paginatedResponse.getPageInfo().isPresent());
        assertFalse(paginatedResponse.getPageInfo().get().isHasNextPage());
        assertEquals("endCursor", paginatedResponse.getPageInfo().get().getEndCursor());
        assertTrue(paginatedResponse.getNextCursor().isPresent());
        assertEquals("endCursor", paginatedResponse.getNextCursor().get());
    }

    @Test
    void testExecuteMultiPageRequest()
            throws Exception {
        when(configuration.getGraphQLApiUrl()).thenReturn(
                "http://localhost:8080/graphql");
        when(configuration.getResilienceConfiguration()).thenReturn(
                resiliencyConfiguration);
        when(resiliencyConfiguration.isEnabled()).thenReturn(false);
        client = new GraphQLClient(configuration, httpClient);
        Mockito.when(httpClient.execute(Mockito.anyString(), Mockito.anyMap(),
                        Mockito.anyString()))
                .thenReturn(new HttpResponse(200, TestUtil.readResourceFile("sample-multi-page-response-1.json")));
        PaginatedGraphQLRequest request = PaginatedGraphQLRequest.builder()
                .tid("tid")
                .authentication(() -> "authHeaderForTesting")
                .query("query-curious")
                .operation("operation-delta")
                .variables(new HashMap<>())
                .httpHeaders(getTestHeaders())
                .pageSizeFieldName("first")
                .pageSize(10)
                .pageInfoJsonPath(PaginatedGraphQLRequestTest.PAGE_INFO_JSON_PATH)
                .endCursorFieldName("after")
                .build();
        PaginatedGraphQLResponse paginatedResponse1 = client.execute(request);
        Assert.assertNotNull(paginatedResponse1);
        assertEquals("test-1", paginatedResponse1.getResponse().extractValue("data.paginatedQuery.node"));
        assertTrue(paginatedResponse1.hasNextPage());
        assertTrue(paginatedResponse1.getPageInfo().isPresent());
        assertTrue(paginatedResponse1.getPageInfo().get().isHasNextPage());
        assertEquals("endCursor-1", paginatedResponse1.getPageInfo().get().getEndCursor());
        assertTrue(paginatedResponse1.getNextCursor().isPresent());
        assertEquals("endCursor-1", paginatedResponse1.getNextCursor().get());
        Assert.assertNotNull(paginatedResponse1.getNextPageRequest());
        assertTrue(paginatedResponse1.getNextPageRequest().isPresent());
        assertEquals("endCursor-1", paginatedResponse1.getNextPageRequest().get().getEndCursor());

        Mockito.when(httpClient.execute(Mockito.anyString(), Mockito.anyMap(),
                        Mockito.anyString()))
                .thenReturn(new HttpResponse(200, TestUtil.readResourceFile("sample-multi-page-response-2.json")));
        Optional<PaginatedGraphQLResponse> paginatedResponse2 = paginatedResponse1.getNextPageResponse(client);
        assertTrue(paginatedResponse2.isPresent());
        Assert.assertNotNull(paginatedResponse2.get());
        assertEquals("test-2", paginatedResponse2.get().getResponse().extractValue("data.paginatedQuery.node"));
        assertFalse(paginatedResponse2.get().hasNextPage());
        assertTrue(paginatedResponse2.get().getPageInfo().isPresent());
        assertFalse(paginatedResponse2.get().getPageInfo().get().isHasNextPage());
        assertEquals("endCursor-2", paginatedResponse2.get().getPageInfo().get().getEndCursor());
        assertTrue(paginatedResponse2.get().getNextCursor().isPresent());
        assertEquals("endCursor-2", paginatedResponse2.get().getNextCursor().get());
        Assert.assertNotNull(paginatedResponse2.get().getNextPageRequest());
        assertEquals("endCursor-1", paginatedResponse1.getNextPageRequest().get().getEndCursor());

        assertFalse(paginatedResponse2.get().getNextPageResponse(client).isPresent());
    }

    @Test
    void testValidExecutePaginatedRequestReturnsResponseWithResiliencyHandler()
            throws Exception {
        CircuitBreakerConfiguration circuitBreakerConfiguration = CircuitBreakerConfiguration.builder()
                .build();
        RetryConfiguration retryConfiguration = RetryConfiguration.builder()
                .build();
        when(configuration.getGraphQLApiUrl()).thenReturn(
                "http://localhost:8080/graphql");
        when(configuration.getResilienceConfiguration()).thenReturn(
                resiliencyConfiguration);
        when(resiliencyConfiguration.isEnabled()).thenReturn(true);
        when(resiliencyConfiguration.getCircuitBreakerConfig()).thenReturn(
                circuitBreakerConfiguration);
        when(resiliencyConfiguration.getRetryConfig()).thenReturn(
                retryConfiguration);
        client = new GraphQLClient(configuration, httpClient);
        Mockito.when(httpClient.execute(Mockito.anyString(), Mockito.anyMap(),
                        Mockito.anyString()))
                .thenReturn(new HttpResponse(200, TestUtil.readResourceFile("sample-paginated-response.json")));
        PaginatedGraphQLRequest request = PaginatedGraphQLRequest.builder()
                .tid("tid")
                .authentication(() -> "authHeaderForTesting")
                .query("query-curious")
                .operation("operation-delta")
                .pageInfoJsonPath(PaginatedGraphQLRequestTest.PAGE_INFO_JSON_PATH)
                .endCursorFieldName("after")
                .httpHeaders(getTestHeaders())
                .build();
        PaginatedGraphQLResponse paginatedResponse = client.execute(request);
        Assert.assertNotNull(paginatedResponse);
        assertEquals("test", paginatedResponse.getResponse().extractValue("data.paginatedQuery.node"));
    }

    @Test
    void testDoesNotThrowWhenEndCursorIsNull() throws Exception {
        when(configuration.getGraphQLApiUrl()).thenReturn(
                "http://localhost:8080/graphql");
        when(configuration.getResilienceConfiguration()).thenReturn(
                resiliencyConfiguration);
        when(resiliencyConfiguration.isEnabled()).thenReturn(false);
        client = new GraphQLClient(configuration, httpClient);
        Mockito.when(httpClient.execute(Mockito.anyString(), Mockito.anyMap(),
                        Mockito.anyString()))
                .thenReturn(new HttpResponse(200, TestUtil.readResourceFile("sample-paginated-response.json")));
        PaginatedGraphQLRequest request1 = PaginatedGraphQLRequest.builder()
                .tid("tid")
                .authentication(() -> "authHeaderForTesting")
                .query("query-curious")
                .operation("operation-delta")
                .pageInfoJsonPath(PaginatedGraphQLRequestTest.PAGE_INFO_JSON_PATH)
                .httpHeaders(getTestHeaders())
                .endCursorFieldName("endCursorFieldName")
                .build();
        assertDoesNotThrow(() -> client.execute(request1));
    }

    @Test
    void testGetNextPageResponseThrowsWhenClientThrows() throws Exception {
        when(configuration.getGraphQLApiUrl()).thenReturn(
                "http://localhost:8080/graphql");
        when(configuration.getResilienceConfiguration()).thenReturn(
                resiliencyConfiguration);
        when(resiliencyConfiguration.isEnabled()).thenReturn(false);
        client = new GraphQLClient(configuration, httpClient);
        Mockito.when(httpClient.execute(Mockito.anyString(), Mockito.anyMap(),
                        Mockito.anyString()))
                .thenReturn(new HttpResponse(200, TestUtil.readResourceFile("sample-multi-page-response-1.json")))
                .thenThrow(new RuntimeException("test exception"));
        PaginatedGraphQLRequest request1 = PaginatedGraphQLRequest.builder()
            .tid("tid")
            .authentication(() -> "authHeaderForTesting")
            .query("query-curious")
            .operation("operation-delta")
            .pageInfoJsonPath(PaginatedGraphQLRequestTest.PAGE_INFO_JSON_PATH)
            .httpHeaders(getTestHeaders())
            .endCursorFieldName("endCursorFieldName")
            .build();
        PaginatedGraphQLResponse response1 = client.execute(request1);
        assertNotNull(response1);
        assertTrue(response1.hasNextPage());
        assertThrows(GraphQLException.class,
            () -> response1.getNextPageResponse(client));
    }

    @Test
    void testWhenOmitNullFieldsFromInputNullFieldsAreRemovedFromInput()
        throws GraphQLException {
        GraphQLRequest request = getTestDataWithOmitEnabled();
        initializeClientWithMockRequestHandler();

        client.execute(request);

        ArgumentCaptor<GraphQLRequest> argumentCaptor = ArgumentCaptor.forClass(
            GraphQLRequest.class);
        verify(mockRequestHandler, times(1)).execute(argumentCaptor.capture());
        Map<String, Object> map = argumentCaptor.getValue().getVariables();
        assertEquals(0, ((Map) map.get("objectWithNull")).size());
        assertEquals(1, ((Map) map.get("objectWithNoNullFields")).size());
    }

    @Test
    void testByDefaultNullFieldsAreNotRemovedFromInput()
        throws GraphQLException {
        GraphQLRequest request = getTestDataWithOmitDisabled();
        initializeClientWithMockRequestHandler();

        client.execute(request);

        ArgumentCaptor<GraphQLRequest> argumentCaptor = ArgumentCaptor.forClass(
            GraphQLRequest.class);
        verify(mockRequestHandler, times(1)).execute(argumentCaptor.capture());
        Map<String, Object> map = argumentCaptor.getValue().getVariables();
        assertEquals(1, ((Map) map.get("objectWithNull")).size());
        assertEquals(1, ((Map) map.get("objectWithNoNullFields")).size());
    }

    private GraphQLRequest getTestDataWithOmitDisabled() {
        return GraphQLRequest.builder()
            .query("query-curious")
            .operation("operation-delta")
            .variables(buildMapFor(
                build("objectWithNull",
                    buildMapFor(Pair.build("nullField", null))),
                build("objectWithNoNullFields",
                    buildMapFor(build("nonNullField", "nonNullValue")))))
            .build();
    }

    private static GraphQLRequest getTestDataWithOmitEnabled() {
        return GraphQLRequest.builder()
            .omitNullFieldsFromInput(true)
            .query("query-curious")
            .operation("operation-delta")
            .variables(buildMapFor(
                build("objectWithNull",
                    buildMapFor(Pair.build("nullField", null))),
                build("objectWithNoNullFields",
                    buildMapFor(build("nonNullField", "nonNullValue")))))
            .build();
    }

    private void initializeClientWithMockRequestHandler() {
        client = new GraphQLClient(configuration, httpClient) {
            @NotNull
            @Override
            protected RequestHandler getRequestHandler(
                GraphQLClientConfiguration configuration,
                RequestExecutor requestExecutor) {
                return mockRequestHandler;
            }
        };
    }

    private Map<String, String> getTestHeaders() {
        HashMap<String, String> headersMap = new HashMap<>();
        headersMap.put("test-header", "test-value");
        return headersMap;
    }
}
