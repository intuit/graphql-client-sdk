package com.intuit.graphql.sdk.client;

import static com.intuit.graphql.sdk.TestUtil.readResourceFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.intuit.graphql.sdk.client.exceptions.GraphQLErrorResponseException;
import com.intuit.graphql.sdk.client.exceptions.GraphQLException;
import com.intuit.graphql.sdk.client.http.GraphQLHTTPClient;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import com.netflix.graphql.dgs.client.HttpResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RequestExecutorTest {

    @Mock
    private GraphQLHTTPClient httpClient;

    @Test
    void testExecuteForSuccessCase() throws GraphQLException {
        Mockito.when(httpClient.execute(Mockito.anyString(), Mockito.anyMap(),
                        Mockito.anyString()))
                .thenReturn(new HttpResponse(200, "{\"data\": {\"test\": \"test\"}}"));
        RequestExecutor requestExecutor = new RequestExecutor(
                "http://localhost:8080/graphql", httpClient);
        Map<String, String> testHeaders = getTestHeaders();
        GraphQLRequest request = GraphQLRequest.builder()
                .tid("tid")
                .authentication(() -> "authHeaderForTesting")
                .query("query-curious")
                .operation("operation-delta")
                .variables(Collections.emptyMap())
                .httpHeaders(testHeaders)
                .build();
        GraphQLResponse response = requestExecutor.executeRequest(request);
        ArgumentCaptor<Map<String, List<String>>> argumentCaptorForHeaders = ArgumentCaptor.forClass(
                Map.class);
        Mockito.verify(httpClient, Mockito.times(1)).execute(
                Mockito.eq("http://localhost:8080/graphql"),
                argumentCaptorForHeaders.capture(),
                Mockito.eq(
                        "{\"query\":\"query-curious\",\"variables\":{},\"operationName\":\"operation-delta\"}"));

        assertEquals("test", response.extractValue("data.test"));
        assertEquals("authHeaderForTesting",
                argumentCaptorForHeaders.getValue().get("Authorization").get(0));
        assertEquals("tid",
                argumentCaptorForHeaders.getValue().get("trace_id").get(0));
        assertEquals("test-value",
                argumentCaptorForHeaders.getValue().get("test-header").get(0));
    }

    @Test
    void testExecuteForSuccessCaseWithNoAuthenticationAndTid()
            throws GraphQLException {
        Mockito.when(httpClient.execute(Mockito.anyString(), Mockito.anyMap(),
                        Mockito.anyString()))
                .thenReturn(new HttpResponse(200, "{\"data\": {\"test\": \"test\"}}"));
        RequestExecutor requestExecutor = new RequestExecutor(
                "http://localhost:8080/graphql", httpClient);

        Map<String, String> testHeaders = getTestHeaders();
        GraphQLRequest request = GraphQLRequest.builder()
                .query("query-curious")
                .operation("operation-delta")
                .variables(Collections.emptyMap())
                .httpHeaders(testHeaders)
                .build();
        GraphQLResponse response = requestExecutor.executeRequest(request);
        ArgumentCaptor<Map<String, List<String>>> argumentCaptorForHeaders = ArgumentCaptor.forClass(
                Map.class);
        Mockito.verify(httpClient, Mockito.times(1)).execute(
                Mockito.eq("http://localhost:8080/graphql"),
                argumentCaptorForHeaders.capture(),
                Mockito.eq(
                        "{\"query\":\"query-curious\",\"variables\":{},\"operationName\":\"operation-delta\"}"));

        assertEquals("test", response.extractValue("data.test"));
        assertEquals("test-value",
                argumentCaptorForHeaders.getValue().get("test-header").get(0));
    }

    @Test
    void testExceptionScenario() {

        Mockito.when(httpClient.execute(Mockito.anyString(), Mockito.anyMap(),
                        Mockito.anyString()))
                .thenThrow(new RuntimeException("test exception"));
        RequestExecutor requestExecutor = new RequestExecutor(
                "http://localhost:8080/graphql", httpClient);
        GraphQLRequest request = GraphQLRequest.builder()
                .tid("tid")
                .authentication(() -> "authHeaderForTesting")
                .query("query-curious")
                .operation("operation-delta")
                .variables(Collections.emptyMap())
                .build();
        Assertions.assertThrows(RuntimeException.class, () ->
                requestExecutor.executeRequest(
                        request));
    }

    @Test
    void shouldThrowExceptionForErrorResponse() throws IOException {
        HttpResponse httpResponse = new HttpResponse(200,
                readResourceFile("error-response-sample.json"));

        Mockito.when(httpClient.execute(Mockito.anyString(), Mockito.anyMap(),
                        Mockito.anyString()))
                .thenReturn(httpResponse);
        RequestExecutor requestExecutor = new RequestExecutor(
                "http://localhost:8080/graphql", httpClient);
        GraphQLRequest request = GraphQLRequest.builder()
                .tid("tid")
                .authentication(() -> "authHeaderForTesting")
                .query("query-curious")
                .operation("operation-delta")
                .variables(Collections.emptyMap())
                .throwExceptionForErrorResponse(true)
                .build();
        Assertions.assertThrows(GraphQLErrorResponseException.class, () ->
                requestExecutor.executeRequest(request));
    }

    @Test
    void shouldThrowExceptionForErrorResponseWithResponseAvailableInException()
            throws IOException {
        HttpResponse httpResponse = new HttpResponse(200,
                readResourceFile("error-response-sample.json"));

        Mockito.when(httpClient.execute(Mockito.anyString(), Mockito.anyMap(),
                        Mockito.anyString()))
                .thenReturn(httpResponse);
        RequestExecutor requestExecutor = new RequestExecutor(
                "http://localhost:8080/graphql", httpClient);

        try {
            requestExecutor.executeRequest(
                    GraphQLRequest.builder()
                            .tid("tid")
                            .authentication(() -> "authHeaderForTesting")
                            .query("query-curious")
                            .operation("operation-delta")
                            .variables(Collections.emptyMap())
                            //.throwExceptionForErrorResponse(true) // default is true
                            .build());
            Assertions.fail("Should have thrown exception");
        } catch (GraphQLErrorResponseException e) {
            assertNotNull(e.getGraphQLResponse());
            assertEquals("Input Format is Incorrect",
                    e.getGraphQLResponse().getErrors().get(0).getMessage());
        } catch (GraphQLException e) {
            Assertions.fail("Should have thrown GraphQLResponseException");
        }
    }

    @Test
    void shouldNotThrowExceptionFromErrorResponseIfDisabled()
            throws GraphQLException, IOException {
        HttpResponse httpResponse = new HttpResponse(200,
                readResourceFile("error-response-sample.json"));

        Mockito.when(httpClient.execute(Mockito.anyString(), Mockito.anyMap(),
                        Mockito.anyString()))
                .thenReturn(httpResponse);
        RequestExecutor requestExecutor = new RequestExecutor(
                "http://localhost:8080/graphql", httpClient);

        GraphQLResponse graphQLResponse = requestExecutor.executeRequest(
                GraphQLRequest.builder()
                        .tid("tid")
                        .authentication(() -> "authHeaderForTesting")
                        .query("query-curious")
                        .operation("operation-delta")
                        .variables(Collections.emptyMap())
                        .throwExceptionForErrorResponse(false)
                        .build());
        assertNotNull(graphQLResponse);
        assertEquals("Input Format is Incorrect",
                graphQLResponse.getErrors().get(0).getMessage());
    }

    private Map<String, String> getTestHeaders() {
        HashMap<String, String> headersMap = new HashMap<>();
        headersMap.put("test-header", "test-value");
        return headersMap;
    }
}
