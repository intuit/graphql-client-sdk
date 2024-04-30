package com.intuit.graphql.sdk.client;

import com.intuit.graphql.sdk.client.exceptions.GraphQLException;
import com.netflix.graphql.dgs.client.GraphQLResponse;

public interface RequestHandler {

    GraphQLResponse execute(GraphQLRequest graphQLRequest)
            throws GraphQLException;
}
