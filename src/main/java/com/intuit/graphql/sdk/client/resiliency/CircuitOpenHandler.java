package com.intuit.graphql.sdk.client.resiliency;

import com.intuit.graphql.sdk.client.GraphQLRequest;
import com.intuit.graphql.sdk.client.exceptions.GraphQLException;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;

public interface CircuitOpenHandler {

    GraphQLResponse handleCallNotPermittedException(GraphQLRequest request,
                                                    CallNotPermittedException cnpe) throws GraphQLException;
}
