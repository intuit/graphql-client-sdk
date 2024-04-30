package com.intuit.graphql.sdk.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.intuit.graphql.sdk.client.DefaultRequestHandler;
import com.intuit.graphql.sdk.client.GraphQLRequest;
import com.intuit.graphql.sdk.client.RequestExecutor;
import com.intuit.graphql.sdk.client.exceptions.GraphQLException;
import com.intuit.graphql.sdk.client.exceptions.GraphQLRuntimeException;
import com.intuit.graphql.sdk.client.exceptions.GraphQLSDKHttpException;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultRequestHandlerTest {

    @Mock
    private RequestExecutor requestExecutor;

    private DefaultRequestHandler defaultRequestHandler;

    @Mock
    GraphQLRequest graphQLRequest;

    GraphQLResponse graphQLResponse = null;

    @BeforeEach
    void setUp() {
        defaultRequestHandler = new DefaultRequestHandler(requestExecutor);
    }

    @Test
    void testExecute() throws GraphQLException {
        Mockito.when(requestExecutor.executeRequest(graphQLRequest))
                .thenReturn(graphQLResponse);
        assertEquals(graphQLResponse,
                defaultRequestHandler.execute(graphQLRequest));
    }

    @Test
    void testExecuteWithGraphQLSDKHttpException()
            throws GraphQLException {
        GraphQLSDKHttpException graphQLSDKHttpException = new GraphQLSDKHttpException(
                500, "INTERNAL_SERVER_ERROR", "test");
        Mockito.when(requestExecutor.executeRequest(graphQLRequest))
                .thenThrow(graphQLSDKHttpException);
        GraphQLException e = Assertions.assertThrows(
                GraphQLException.class,
                () -> defaultRequestHandler.execute(graphQLRequest));
        assertEquals(graphQLSDKHttpException, e.getCause());
    }

    @Test
    void testExecuteWithGraphQLRuntimeException()
            throws GraphQLException {
        GraphQLRuntimeException graphQLRuntimeException = new GraphQLRuntimeException(
                new Throwable());
        Mockito.when(requestExecutor.executeRequest(graphQLRequest))
                .thenThrow(graphQLRuntimeException);
        GraphQLException e = Assertions.assertThrows(
                GraphQLException.class,
                () -> defaultRequestHandler.execute(graphQLRequest));
        assertEquals(graphQLRuntimeException, e.getCause());
    }

    @Test
    void testExecuteWithException() throws GraphQLException {
        RuntimeException runtimeException = new RuntimeException();
        Mockito.when(requestExecutor.executeRequest(graphQLRequest))
                .thenThrow(runtimeException);
        GraphQLException e = Assertions.assertThrows(
                GraphQLException.class,
                () -> defaultRequestHandler.execute(graphQLRequest));
        assertEquals(runtimeException, e.getCause());
    }

}

