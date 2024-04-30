package com.intuit.graphql.sdk.client.resiliency;

import com.intuit.graphql.sdk.client.GraphQLRequest;
import com.intuit.graphql.sdk.client.exceptions.GraphQLException;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;

public class DefaultCircuitOpenHandler implements CircuitOpenHandler {

    public GraphQLResponse handleCallNotPermittedException(
        GraphQLRequest request,
                                                           CallNotPermittedException cnpe) throws GraphQLException {
        throw new GraphQLException(cnpe);
    }
}
