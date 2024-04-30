package com.intuit.graphql.sdk.client.cache;

import static io.github.resilience4j.core.StringUtils.isNotEmpty;

import com.intuit.graphql.sdk.client.GraphQLRequest;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.jetbrains.annotations.NotNull;

public class DefaultCacheKeyGenerator implements CacheKeyGenerator {

    private final String url;
    private final List<String> headersToBeIncluded;

    public DefaultCacheKeyGenerator(String graphQlApiUrl,
                                    List<String> headersToBeIncluded) {
        this.url = graphQlApiUrl;
        this.headersToBeIncluded = new LinkedList<>();
        headersToBeIncluded.forEach(
                e -> this.headersToBeIncluded.add(e.toLowerCase()));
        this.headersToBeIncluded.add("authorization"); // It will always be added.
    }

    @Override
    public String generateCacheKey(GraphQLRequest graphQLRequest) {
        List<String> result = new LinkedList<>();
        result.add(getKeySubset("url", this.url));
        result.add(getKeySubset("operation", graphQLRequest.getOperation()));
        result.add(getKeySubset("query", graphQLRequest.getQuery()));
        result.add(getKeySubset("variables", convertMapToString(graphQLRequest)));
        result.add(getKeySubset("headers", getHeaders(graphQLRequest)));
        return hashKey(String.join(" $ ", result));
    }

    private static String convertMapToString(GraphQLRequest graphQLRequest) {
        return String.valueOf(graphQLRequest.getVariables());
    }

    private String getHeaders(GraphQLRequest graphQLRequest) {
        List<String> headers = new LinkedList<>();
        if (graphQLRequest.getAuthentication() != null &&
                isNotEmpty(
                        graphQLRequest.getAuthentication().getAuthorizationHeader())) {
            headers.add(getKeySubset("authorization",
                    graphQLRequest.getAuthentication()
                            .getAuthorizationHeader()));
        }
        headersToBeIncluded.forEach(k -> {
            if (graphQLRequest.getHttpHeaders().containsKey(k)) {
                headers.add(getKeySubset(k, graphQLRequest.getHttpHeaders().get(k)));
            }
        });
        return String.join(";", headers);
    }

    @NotNull
    private static String getKeySubset(String key, String value) {
        return key + ": " + value;
    }

    String hashKey(String plainKey) {
        return DigestUtils.sha256Hex(plainKey);
    }
}
