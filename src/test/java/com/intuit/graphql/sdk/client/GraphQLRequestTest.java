package com.intuit.graphql.sdk.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.intuit.graphql.sdk.client.GraphQLRequest.GraphQLRequestBuilder;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GraphQLRequestTest {

    public static final TestAuth AUTHENTICATION = new TestAuth("appId",
            "secret");
    public static final String TID = "tid";
    public static final String QUERY = "query";
    public static final String OPERATION = "operation";
    public static final String EXPECTED_AUTH_HEADER_VALUE = "appId:secret";

    @Test
    void testGraphQLRequestConstructionUsingABuilder() {
        GraphQLRequest request = GraphQLRequest.builder()
            .authentication(AUTHENTICATION)
            .query(QUERY)
            .tid(TID)
            .operation(OPERATION)
            .variables(Collections.emptyMap())
            .httpHeaders(Collections.emptyMap())
            .omitNullFieldsFromInput(true)
            .build();
        assertEquals(QUERY, request.getQuery());
        assertEquals(TID, request.getTid());
        assertEquals(EXPECTED_AUTH_HEADER_VALUE,
            request.getAuthentication().getAuthorizationHeader());
        assertEquals(OPERATION, request.getOperation());
        assertEquals(0, request.getVariables().size());
        assertTrue(request.isOmitNullFieldsFromInput());
    }

    @Test
    void testMandatoryArgumentsViaBuilder() {
        GraphQLRequest request = GraphQLRequest.builder()
                .authentication(AUTHENTICATION)
                .query(QUERY)
                .tid(TID)
                .build();
        assertEquals(QUERY, request.getQuery());
        assertEquals(TID, request.getTid());
        assertEquals(EXPECTED_AUTH_HEADER_VALUE,
                request.getAuthentication().getAuthorizationHeader());
        assertNull(request.getOperation());
        assertNull(request.getVariables());
    }

    @Test
    void testExceptionForMissingMandatoryArgumentQuery() {
        GraphQLRequestBuilder requestBuilder = GraphQLRequest.builder()
                .tid(TID)
                .authentication(AUTHENTICATION);
        Assertions.assertThrows(NullPointerException.class,
                requestBuilder::build);
    }
}