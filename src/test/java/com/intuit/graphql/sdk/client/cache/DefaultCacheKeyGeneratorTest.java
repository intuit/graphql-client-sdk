package com.intuit.graphql.sdk.client.cache;

import static com.intuit.graphql.sdk.client.util.Pair.build;
import static com.intuit.graphql.sdk.client.util.Pair.buildMapFor;
import static org.junit.jupiter.api.Assertions.*;

import com.intuit.graphql.sdk.client.GraphQLRequest;

import com.intuit.graphql.sdk.client.TestAuth;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultCacheKeyGeneratorTest {

    private DefaultCacheKeyGenerator cacheKeyGenerator;

    @BeforeEach
    void setUp() {
        cacheKeyGenerator = new DefaultCacheKeyGenerator("https://www.example.org/v1/graphql",
                Collections.singletonList("intuit-company-id")) {
            @Override
            String hashKey(String plainKey) {
                return plainKey;  // For testing.
            }
        };
    }

    @Test
    void testCacheKeyGeneration() {
        String cacheKey = cacheKeyGenerator.generateCacheKey(
                GraphQLRequest.builder()
                        .query("query-data")
                        .operation("operation")
                        .variables(
                                buildMapFor(build("variableName", "variableValue"), build("variable2", "variable2value")))
                        .httpHeaders(buildMapFor(
                                build("intuit-company-id", "company123"),
                                build("refer-id", "12323")))
                        .build());
        assertNotNull(cacheKey);
        assertEquals("url: https://www.example.org/v1/graphql $ operation: operation $ query: query-data $ variables: {variableName=variableValue, variable2=variable2value} $ headers: intuit-company-id: company123", cacheKey);
    }


    @Test
    void testWhenVariableAreNotSet() {
        String cacheKey = cacheKeyGenerator.generateCacheKey(
                GraphQLRequest.builder()
                        .query("query-data")
                        .operation("operation")
                        .httpHeaders(buildMapFor(
                                build("intuit-company-id", "company123"),
                                build("refer-id", "12323")))
                        .build());
        assertNotNull(cacheKey);
        assertEquals("url: https://www.example.org/v1/graphql $ operation: operation $ query: query-data $ variables: null $ headers: intuit-company-id: company123", cacheKey);
    }

    @Test
    void testWhenHeadersAreNotSet() {
        String cacheKey = cacheKeyGenerator.generateCacheKey(
                GraphQLRequest.builder()
                        .query("query-data")
                        .operation("operation")
                        .build());
        assertNotNull(cacheKey);
        assertEquals("url: https://www.example.org/v1/graphql $ operation: operation $ query: query-data $ variables: null $ headers: ", cacheKey);
    }

    @Test
    void testWhenAuthenticationIsSetWithCustomAuth() {
        String cacheKey = cacheKeyGenerator.generateCacheKey(
                GraphQLRequest.builder()
                        .query("query-data")
                        .operation("operation")
                        .authentication(new TestAuth("appId", "appSecret"))
                        .build());
        assertNotNull(cacheKey);
        assertEquals("url: https://www.example.org/v1/graphql $ operation: operation $ query: query-data $ variables: null $ headers: authorization: appId:appSecret", cacheKey);
    }


    @Test
    void testHashFunction() {
        DefaultCacheKeyGenerator generator = new DefaultCacheKeyGenerator("someUrl", Collections.emptyList());
        String testUser1 = generator.hashKey("testUser1");
        String testUser2 = generator.hashKey("testUser2");
        assertNotNull(testUser1);
        assertNotNull(testUser2);
        assertNotEquals(testUser1, testUser2);
        assertEquals(testUser1.length(), testUser2.length());
        assertEquals("41a6d34fb5d74ebb33ef26c46b93cb7dcd067d7955e7bf0760f84e0cf7d259cf", testUser1);
        assertEquals("de0a5ee1955ac51bde99feca3537e6eb2dd6341bb585bb3ee4c45d49689656cb", testUser2);
    }

}
