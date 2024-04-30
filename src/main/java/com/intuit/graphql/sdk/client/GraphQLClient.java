package com.intuit.graphql.sdk.client;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.graphql.sdk.client.config.GraphQLClientConfiguration;
import com.intuit.graphql.sdk.client.exceptions.GraphQLException;
import com.intuit.graphql.sdk.client.http.GraphQLHTTPClient;
import com.intuit.graphql.sdk.client.http.GraphQLHttpClientBuilder;
import com.intuit.graphql.sdk.client.util.LogEvent.LogEventName;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a wrapper around the DGS CustomGraphQLClient. It is used to inject resiliency and other functionalities while executing requests and adds easy configuration of Authorization header and intuit_tid.
 */
public class GraphQLClient {

    private final String graphQLApiUrl;

    private static final Logger log = LoggerFactory.getLogger(
        GraphQLClient.class);

    private final RequestHandler requestHandler;

    // Initialize it once to reuse it
    private static final ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(
        Include.NON_NULL);

    public GraphQLClient(GraphQLClientConfiguration configuration) {
        this(configuration, new GraphQLHttpClientBuilder(
            configuration.getHttpConfiguration()).build());
    }

    public GraphQLClient(GraphQLClientConfiguration configuration,
        GraphQLHTTPClient httpClient) {
        this.graphQLApiUrl = configuration.getGraphQLApiUrl();
        RequestExecutor requestExecutor = new RequestExecutor(graphQLApiUrl,
            httpClient);
        RequestHandler tempRequestHandler = getRequestHandler(
            configuration, requestExecutor);

        if (configuration.getCacheConfiguration() != null) {
            requestHandler = new CacheRequestHandler(
                tempRequestHandler,
                configuration.getCacheConfiguration().getGraphQLCache(),
                configuration.getCacheConfiguration().getCacheKeyGenerator()
            );
        } else {
            requestHandler = tempRequestHandler;
        }
    }

    // Made it protected to reduce the unit testing effort. Now we can override this function and just mock the RequestHandler object.
    @NotNull
    protected RequestHandler getRequestHandler(
        GraphQLClientConfiguration configuration,
        RequestExecutor requestExecutor) {
        RequestHandler tempRequestHandler;
        if (configuration.getResilienceConfiguration().isEnabled()) {
            tempRequestHandler = new ResiliencyRequestHandler(configuration,
                requestExecutor);
        } else {
            tempRequestHandler = new DefaultRequestHandler(requestExecutor);
        }
        return tempRequestHandler;
    }

    public GraphQLResponse execute(GraphQLRequest request)
        throws GraphQLException {
        request.getLogEvent().startTime();
        try {
            omitNullFieldsFromInput(request);
            addDetailsInLogEvent(request);
            return requestHandler.execute(request);
        } finally {
            request.getLogEvent().stopTime();
            if (log.isInfoEnabled()) {
                log.info(request.getLogEvent().getLogEntry());
            }
        }
    }

    private void omitNullFieldsFromInput(GraphQLRequest request) {
        if (!request.isOmitNullFieldsFromInput()) {
            return; // Omit is disabled
        }
        Map<String, Object> variables = request.getVariables();
        for (Entry<String, Object> entry : variables.entrySet()) {
            Object cleanedObject = removeNullFieldsFromInput(entry.getValue());
            if (cleanedObject != null) {
                request.getVariables().put(entry.getKey(), cleanedObject);
            }
        }

    }

    private Object removeNullFieldsFromInput(Object input) {
        try {
            return objectMapper.readValue(
                objectMapper.writeValueAsString(input),
                Object.class
            );
        } catch (IOException e) {
            log.error("Error while removing null fields from input: {}", input,
                e);
            return input;
        }
    }

    public PaginatedGraphQLResponse execute(PaginatedGraphQLRequest request)
        throws GraphQLException {
        GraphQLResponse response = this.execute((GraphQLRequest) request);
        return new PaginatedGraphQLResponse(
            request,
            response
        );
    }

    protected void addDetailsInLogEvent(GraphQLRequest request) {
        request.addToLogEvent(LogEventName.URL, graphQLApiUrl);
        request.addToLogEvent(LogEventName.TRANSACTION_ID, request.getTid());
        request.addToLogEvent(LogEventName.HEADERS, getAllHeadersForLogging(request));
        request.addToLogEvent(LogEventName.QUERY, request.getQuery());
        request.addToLogEvent(LogEventName.VARIABLES, request.getVariables());
        request.addToLogEvent(LogEventName.OPERATION, request.getOperation());
    }

    private static Map<String, String> getAllHeadersForLogging(
            GraphQLRequest request) {
        Map<String, String> logHeaders = new HashMap<>();
        if (request.getAuthentication() != null) {
            logHeaders.put(RequestExecutor.HTTP_HEADER_AUTHORIZATION,
                    request.getAuthentication().getAuthorizationHeader());
        }

        if (request.getHttpHeaders() != null) {
            logHeaders.putAll(request.getHttpHeaders());
        }
        return logHeaders;
    }
}