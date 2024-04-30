package com.intuit.graphql.sdk.client;

import com.intuit.graphql.sdk.client.cache.CacheKeyGenerator;
import com.intuit.graphql.sdk.client.cache.GraphQLCache;
import com.intuit.graphql.sdk.client.cache.SerializableGraphQLResponse;
import com.intuit.graphql.sdk.client.exceptions.GraphQLException;
import com.intuit.graphql.sdk.client.util.LogEvent.LogEventName;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

public class CacheRequestHandler implements
        RequestHandler {

    // The request handler when cache is not available or disabled for the current request.
    private final RequestHandler requestHandler;
    private final GraphQLCache graphQLCache;
    private final CacheKeyGenerator cacheKeyGenerator;

    public CacheRequestHandler(
            RequestHandler requestHandler,
            GraphQLCache graphQLCache,
            CacheKeyGenerator cacheKeyGenerator) {
        this.requestHandler = requestHandler;
        this.graphQLCache = graphQLCache;
        this.cacheKeyGenerator = cacheKeyGenerator;
    }


    @Override
    public GraphQLResponse execute(GraphQLRequest graphQLRequest)
            throws GraphQLException {
        if (isCacheDisabled(graphQLRequest)) {
            return getResponseFromApi(graphQLRequest);
        }
        return getResponseViaCache(graphQLRequest);
    }

    private GraphQLResponse getResponseViaCache(GraphQLRequest graphQLRequest)
            throws GraphQLException {
        try {
            Optional<GraphQLResponse> response = getFromCache(graphQLRequest);
            if (response.isPresent()) {
                return response.get();
            }
        } catch (Exception e) {
            addCacheStatusToLog(graphQLRequest, "failed:" + e);
        }
        return getResponseFromApiAndSetInTheCache(graphQLRequest);
    }

    private GraphQLResponse getResponseFromApiAndSetInTheCache(GraphQLRequest graphQLRequest)
            throws GraphQLException {
        GraphQLResponse apiResponse = requestHandler.execute(graphQLRequest);
        setResponseInCache(graphQLRequest, apiResponse);
        return apiResponse;
    }

    @NotNull
    private Optional<GraphQLResponse> getFromCache(
            GraphQLRequest graphQLRequest) {
        Optional<SerializableGraphQLResponse> response = graphQLCache.get(
                graphQLRequest,
                cacheKeyGenerator.generateCacheKey(graphQLRequest)
        );
        addCacheStatusToLog(graphQLRequest, response.isPresent() ? "hit" : "miss");
        return response.map(SerializableGraphQLResponse::getGraphQLResponse);
    }

    private void setResponseInCache(GraphQLRequest graphQLRequest,
                                    GraphQLResponse apiResponse) {
        try {
            graphQLCache.set(
                    graphQLRequest,
                    cacheKeyGenerator.generateCacheKey(graphQLRequest),
                    new SerializableGraphQLResponse(apiResponse),
                    graphQLRequest.getCacheBehavior().getTtl(),
                    graphQLRequest.getCacheBehavior().getTimeUnit()
            );
        } catch (Exception e) {
            addCacheStatusToLog(graphQLRequest, "miss+setFailed:" + e);
        }
    }

    private GraphQLResponse getResponseFromApi(GraphQLRequest graphQLRequest)
            throws GraphQLException {
        addCacheStatusToLog(graphQLRequest, "disabled");
        return requestHandler.execute(graphQLRequest);
    }

    private void addCacheStatusToLog(GraphQLRequest graphQLRequest, String status) {
        graphQLRequest.getLogEvent().add(LogEventName.CACHE_STATUS, status);
    }

    private static boolean isCacheDisabled(GraphQLRequest graphQLRequest) {
        return graphQLRequest.getCacheBehavior() == null
                || !graphQLRequest.getCacheBehavior().isCacheEnabled();
    }
}
