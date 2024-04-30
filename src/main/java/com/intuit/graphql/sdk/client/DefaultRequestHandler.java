package com.intuit.graphql.sdk.client;

import com.intuit.graphql.sdk.client.exceptions.GraphQLException;
import com.netflix.graphql.dgs.client.GraphQLResponse;

public class DefaultRequestHandler implements RequestHandler {

    private final RequestExecutor requestExecutor;

    public DefaultRequestHandler(RequestExecutor requestExecutor) {
        this.requestExecutor = requestExecutor;
    }

    public GraphQLResponse execute(GraphQLRequest graphQLRequest)
            throws GraphQLException {
        try {
            return requestExecutor.executeRequest(graphQLRequest);
        } catch (GraphQLException e) {
            throw e;
        } catch (Exception e) {
            throw new GraphQLException(e);
        }
    }
}
