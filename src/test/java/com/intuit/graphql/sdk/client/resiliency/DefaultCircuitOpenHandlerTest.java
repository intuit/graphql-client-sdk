package com.intuit.graphql.sdk.client.resiliency;

import static org.mockito.Mockito.when;

import com.intuit.graphql.sdk.client.GraphQLRequest;
import com.intuit.graphql.sdk.client.exceptions.GraphQLException;
import com.intuit.graphql.sdk.client.resiliency.DefaultCircuitOpenHandler;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultCircuitOpenHandlerTest {

    GraphQLRequest graphQLRequest;
    @Mock
    CircuitBreaker circuitBreaker = null;
    @Mock
    CircuitBreakerConfig circuitBreakerConfig;

    @Test
    void testWithCallNotPermittedException() {
        when(circuitBreaker.getName()).thenReturn("test");
        when(circuitBreaker.getState()).thenReturn(CircuitBreaker.State.OPEN);
        when(circuitBreaker.getCircuitBreakerConfig()).thenReturn(
                circuitBreakerConfig);
        when(circuitBreakerConfig.isWritableStackTraceEnabled()).thenReturn(true);
        DefaultCircuitOpenHandler defaultCircuitOpenHandler = new DefaultCircuitOpenHandler();
        Assertions.assertThrows(GraphQLException.class,
                () -> defaultCircuitOpenHandler.handleCallNotPermittedException(
                        graphQLRequest,
                        CallNotPermittedException.createCallNotPermittedException(
                                circuitBreaker)));
    }
}
