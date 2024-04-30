package com.intuit.graphql.sdk.client.cache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.intuit.graphql.sdk.TestUtil;
import com.netflix.graphql.dgs.client.GraphQLResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

class SerializableGraphQLResponseTest {

    @Test
    void testConversionFromGraphQLResponse() throws IOException {
        GraphQLResponse testGraphQLResponse = getSampleGraphQLResponse();
        SerializableGraphQLResponse serializableGraphQLResponse = new SerializableGraphQLResponse(testGraphQLResponse);

        GraphQLResponse convertedGraphQLResponse = serializableGraphQLResponse.getGraphQLResponse();
        assertSame(testGraphQLResponse, convertedGraphQLResponse);
    }

    @Test
    void testSerialization() throws IOException {
        GraphQLResponse sampleGraphQLResponse = getSampleGraphQLResponse();
        SerializableGraphQLResponse serializable = new SerializableGraphQLResponse(
                sampleGraphQLResponse);

        byte[] serialized = TestUtil.serialize(serializable);
        Object deserialize = TestUtil.deserialize(serialized);
        assertTrue(deserialize instanceof SerializableGraphQLResponse);
        SerializableGraphQLResponse deserialized = (SerializableGraphQLResponse) deserialize;
        assertNotSame(sampleGraphQLResponse, deserialized.getGraphQLResponse());
        assertSame(deserialized.getGraphQLResponse(), deserialized.getGraphQLResponse());
        assertEquals(sampleGraphQLResponse.getJson(), deserialized.getGraphQLResponse().getJson());
        assertEquals(sampleGraphQLResponse.getHeaders(), deserialized.getGraphQLResponse().getHeaders());
    }

    private static GraphQLResponse getSampleGraphQLResponse() throws IOException {
        HashMap<String, List<String>> headers = new HashMap<>();
        headers.put("testKey1", Collections.singletonList("testValue1"));
        headers.put("testKey2", Collections.singletonList("testValue2"));
        return new GraphQLResponse(TestUtil.readResourceFile("sample-response.json"),
                headers);
    }

}