package com.intuit.graphql.sdk.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.intuit.graphql.sdk.client.config.CircuitBreakerConfiguration;
import com.intuit.graphql.sdk.client.config.GraphQLClientConfiguration;
import com.intuit.graphql.sdk.client.config.ResiliencyConfiguration;
import com.intuit.graphql.sdk.client.config.RetryConfiguration;
import com.intuit.graphql.sdk.client.exceptions.GraphQLErrorResponseException;
import com.intuit.graphql.sdk.client.exceptions.GraphQLException;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResiliencyRequestHandlerTest {

    @Mock
    private GraphQLClientConfiguration configuration;
    @Mock
    private RequestExecutor requestExecutor;
    @Mock
    private CircuitBreaker circuitBreaker;
    @Mock
    CircuitBreakerConfig circuitBreakerConfig;
    @Mock
    ResiliencyConfiguration resiliencyConfiguration;
    GraphQLRequest request;
    PaginatedGraphQLRequest paginatedRequest;

    @BeforeEach
    void setUp() {
        CircuitBreakerConfiguration circuitBreakerConfiguration = CircuitBreakerConfiguration.builder()
                .build();
        RetryConfiguration retryConfiguration = RetryConfiguration.builder()
                .build();
        when(configuration.getResilienceConfiguration()).thenReturn(
                resiliencyConfiguration);
        when(resiliencyConfiguration.getCircuitBreakerConfig()).thenReturn(
                circuitBreakerConfiguration);
        when(resiliencyConfiguration.getRetryConfig()).thenReturn(
                retryConfiguration);
        request = GraphQLRequest.builder().query(
                GraphQLRequestTest.QUERY).build();
        paginatedRequest = PaginatedGraphQLRequest.builder().query(GraphQLRequestTest.QUERY)
                .pageInfoJsonPath(PaginatedGraphQLRequestTest.PAGE_INFO_JSON_PATH)
                .endCursorFieldName(PaginatedGraphQLRequestTest.END_CURSOR_FIELD_NAME)
                .build();
    }

    @Test
    void testExecuteSuccessfulResponse() throws Exception {
        GraphQLResponse response = null;
        ResiliencyRequestHandler resiliencyHandler = new ResiliencyRequestHandler(
                configuration, requestExecutor);
        when(requestExecutor.executeRequest(request)).thenReturn(response);
        GraphQLResponse result = resiliencyHandler.execute(request);
        assertEquals(response, result);
    }

    @Test
    void testExecuteCircuitOpenHandlerCalled() throws Exception {
        when(circuitBreaker.getName()).thenReturn("test");
        when(circuitBreaker.getState()).thenReturn(CircuitBreaker.State.OPEN);
        when(circuitBreaker.getCircuitBreakerConfig()).thenReturn(
                circuitBreakerConfig);
        when(circuitBreakerConfig.isWritableStackTraceEnabled()).thenReturn(true);
        CallNotPermittedException callNotPermittedException = CallNotPermittedException.createCallNotPermittedException(
                circuitBreaker);
        ResiliencyRequestHandler resiliencyHandler = new ResiliencyRequestHandler(
                configuration, requestExecutor);
        Mockito.when(requestExecutor.executeRequest(request))
                .thenThrow(callNotPermittedException);
        GraphQLException e = assertThrows(GraphQLException.class,
                () -> resiliencyHandler.execute(request));
        assertEquals(callNotPermittedException, e.getCause());
    }

    @Test
    void testExecuteGraphQLExceptionThrown() throws Exception {
        RuntimeException exception = new RuntimeException("Some error");
        Mockito.when(requestExecutor.executeRequest(request))
                .thenThrow(exception);
        ResiliencyRequestHandler resiliencyHandler = new ResiliencyRequestHandler(
                configuration, requestExecutor);
        GraphQLException e = assertThrows(GraphQLException.class,
                () -> resiliencyHandler.execute(request));
        assertEquals(exception, e.getCause());
    }

    @Test
    void testExecuteGraphQLErrorResponseExceptionThrown()
            throws Exception {
        GraphQLResponse response = null;
        GraphQLErrorResponseException exception = new GraphQLErrorResponseException(
                response);
        Mockito.when(requestExecutor.executeRequest(request))
                .thenThrow(exception);
        ResiliencyRequestHandler resiliencyHandler = new ResiliencyRequestHandler(
                configuration, requestExecutor);
        assertThrows(GraphQLException.class,
                () -> resiliencyHandler.execute(request));
    }
}
