package com.intuit.graphql.sdk.client;

import com.intuit.graphql.sdk.client.CacheRequestHandler;
import com.intuit.graphql.sdk.client.GraphQLRequest;
import com.intuit.graphql.sdk.client.RequestHandler;
import com.intuit.graphql.sdk.client.cache.CacheBehavior;
import com.intuit.graphql.sdk.client.cache.CacheKeyGenerator;
import com.intuit.graphql.sdk.client.cache.DefaultCacheKeyGenerator;
import com.intuit.graphql.sdk.client.cache.GraphQLCache;
import com.intuit.graphql.sdk.client.cache.SerializableGraphQLResponse;
import com.intuit.graphql.sdk.client.exceptions.GraphQLException;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CacheRequestHandlerTest {


    public static final String CACHE_KEY = "7c2698e1bc37c4961aaf32a413b08a24a08c7d036081596797461ff6d7ef8a0d";
    @Mock
    private RequestHandler mockRequestHandler;
    @Mock
    private GraphQLCache mockCache;

    private CacheRequestHandler cacheRequestHandler;
    private GraphQLRequest request;
    private GraphQLResponse expectedResponse;
    private final CacheKeyGenerator cacheKeyGenerator = new DefaultCacheKeyGenerator("https://www.example.org/v1/graphql",
            Collections.emptyList());

    @BeforeEach
    void setUp() {
        cacheRequestHandler = new CacheRequestHandler(mockRequestHandler, mockCache, cacheKeyGenerator);
        expectedResponse = new GraphQLResponse("{}");
    }

    @Test
    void testWhenCacheIsDisabledNoCallToCache() throws GraphQLException {
        when(
                mockRequestHandler.execute(any()))
                .thenReturn(expectedResponse);
        request = GraphQLRequest.builder().cacheBehavior(
                        CacheBehavior.builder().isCacheEnabled(false).build()).query("query")
                .build();
        GraphQLResponse response = cacheRequestHandler.execute(
                request);
        verifyNoInteractions(mockCache);
        assertEquals(expectedResponse, response);
        verifyTheCacheStatusIs("disabled");
    }

    @Test
    void testWhenCacheHitHappens() throws GraphQLException {
        when(mockCache.get(any(), eq(CACHE_KEY)))
                .thenReturn(Optional.of(new SerializableGraphQLResponse(expectedResponse)));
        request = GraphQLRequest.builder().cacheBehavior(
                        CacheBehavior.builder().isCacheEnabled(true).ttl(100).timeUnit(
                                TimeUnit.MINUTES).build()).query("query")
                .build();
        GraphQLResponse response = cacheRequestHandler.execute(
                request);
        verifyNoInteractions(mockRequestHandler);
        assertEquals(expectedResponse, response);
        verifyTheCacheStatusIs("hit");
    }

    @Test
    void testWhenCacheMissHappens() throws GraphQLException {
        when(mockCache.get(any(), eq(CACHE_KEY)))
                .thenReturn(Optional.empty());
        when(
                mockRequestHandler.execute(any()))
                .thenReturn(expectedResponse);
        request = GraphQLRequest.builder().cacheBehavior(
                        CacheBehavior.builder().isCacheEnabled(true).ttl(100).timeUnit(
                                TimeUnit.MINUTES).build()).query("query")
                .build();
        GraphQLResponse response = cacheRequestHandler.execute(
                request);
        assertEquals(expectedResponse, response);
        verify(mockCache).set(any(), eq(CACHE_KEY), any(SerializableGraphQLResponse.class), eq(100), eq(TimeUnit.MINUTES));
        verifyTheCacheStatusIs("miss");
    }

    @Test
    void testWhenCacheThrowsException() throws GraphQLException {
        when(mockCache.get(any(), eq(CACHE_KEY)))
                .thenThrow(new RuntimeException("Test exception"));
        when(
                mockRequestHandler.execute(any()))
                .thenReturn(expectedResponse);
        request = GraphQLRequest.builder().cacheBehavior(
                        CacheBehavior.builder().isCacheEnabled(true).ttl(100).timeUnit(
                                TimeUnit.MINUTES).build()).query("query")
                .build();
        GraphQLResponse response = cacheRequestHandler.execute(
                request);
        assertEquals(expectedResponse, response);
        verifyTheCacheStatusIs("failed:java.lang.RuntimeException: Test exception");
    }

    @Test
    void testWhenCacheSetThrowsException() throws GraphQLException {
        when(mockCache.get(any(), eq(CACHE_KEY)))
                .thenReturn(Optional.empty());
        when(
                mockRequestHandler.execute(any()))
                .thenReturn(expectedResponse);
        doThrow(new RuntimeException("Test exception")).when(mockCache).set(
                any(), eq(CACHE_KEY), any(SerializableGraphQLResponse.class), eq(100), eq(TimeUnit.MINUTES));
        request = GraphQLRequest.builder().cacheBehavior(
                        CacheBehavior.builder().isCacheEnabled(true).ttl(100).timeUnit(
                                TimeUnit.MINUTES).build()).query("query")
                .build();
        GraphQLResponse response = cacheRequestHandler.execute(
                request);
        assertEquals(expectedResponse, response);

        verifyTheCacheStatusIs("miss+setFailed:java.lang.RuntimeException: Test exception");
    }

    @Test
    void testWhenApiThrowsException() throws GraphQLException {
        when(mockCache.get(any(), eq(CACHE_KEY)))
                .thenReturn(Optional.empty());
        when(
                mockRequestHandler.execute(any()))
                .thenThrow(new GraphQLException("Test exception"));
        request = GraphQLRequest.builder().cacheBehavior(
                        CacheBehavior.builder().isCacheEnabled(true).ttl(100).timeUnit(
                                TimeUnit.MINUTES).build()).query("query")
                .build();

        assertThrows(GraphQLException.class, () -> cacheRequestHandler.execute(
                request));
        verifyNoMoreInteractions(mockCache);
        verifyTheCacheStatusIs("miss");
    }

    @Test
    void testWhenThereIsCacheContext() throws GraphQLException {
        when(mockCache.get(any(), any())).thenReturn(Optional.empty());
        when(mockRequestHandler.execute(any())).thenReturn(expectedResponse);
        final Map<String, String> cacheContext = new HashMap<>();
        cacheContext.put("testKey", "testValue");
        request = GraphQLRequest.builder().cacheBehavior(
                CacheBehavior.builder()
                        .cacheContext(cacheContext)
                        .isCacheEnabled(true)
                        .ttl(100)
                        .timeUnit(TimeUnit.MINUTES).build()
        ).query("query").build();
        cacheRequestHandler.execute(request);
        ArgumentCaptor<GraphQLRequest> graphQLRequestCaptor = ArgumentCaptor.forClass(GraphQLRequest.class);
        verify(mockCache, times(1)).get(graphQLRequestCaptor.capture(), any());
        assertEquals("testValue", graphQLRequestCaptor.getValue().getCacheBehavior().getCacheContext().get("testKey"));
        verify(mockCache, times(1)).set(graphQLRequestCaptor.capture(), any(), any(), anyInt(), any());
        assertEquals("testValue", graphQLRequestCaptor.getValue().getCacheBehavior().getCacheContext().get("testKey"));
    }


    private void verifyTheCacheStatusIs(String status) {
        assertEquals("cacheStatus=" + status, request.getLogEvent().getLogEntry());
    }
}