package com.intuit.graphql.sdk.client;

import static com.intuit.graphql.sdk.client.util.LogEvent.LogEventName.RESPONSE;
import static java.util.Collections.singletonList;

import com.intuit.graphql.sdk.client.exceptions.GraphQLErrorResponseException;
import com.intuit.graphql.sdk.client.http.GraphQLHTTPClient;
import com.intuit.graphql.sdk.client.util.UserAgentSupplier;
import com.netflix.graphql.dgs.client.GraphQLClient;
import com.netflix.graphql.dgs.client.GraphQLResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

public class RequestExecutor {

    private final String graphQLApiUrl;
    private final GraphQLHTTPClient httpClient;
    public static final String HTTP_HEADER_TRACE_ID = "trace_id";
    public static final String HTTP_HEADER_AUTHORIZATION = "Authorization";

    public RequestExecutor(String graphQLApiUrl, GraphQLHTTPClient httpClient) {
        this.graphQLApiUrl = graphQLApiUrl;
        this.httpClient = httpClient;
    }

    @NotNull
    public GraphQLResponse executeRequest(GraphQLRequest request)
            throws GraphQLErrorResponseException {
        GraphQLResponse response = GraphQLClient
                .createCustom(
                        graphQLApiUrl,
                        (url, headers, body) ->
                                httpClient.execute(
                                        url,
                                        getHeaders(request, headers),
                                        body)
                )
                .executeQuery(request.getQuery(), request.getVariables(),
                        request.getOperation());
        request.addToLogEvent(RESPONSE, response.getJson());
        if (response.hasErrors() && request.isThrowExceptionForErrorResponse()) {
            throw new GraphQLErrorResponseException(response);
        }
        return response;
    }

    @NotNull
    private Map<String, List<String>> getHeaders(GraphQLRequest request,
                                                 Map<String, ? extends List<String>> headers) {
        Map<String, List<String>> headersNew = new HashMap<>(headers);
        addAuthorizationHeaderIfAvailable(request, headersNew);
        addIntuitTidIfAvailable(request, headersNew);
        request.getHttpHeaders()
                .forEach((key, val) -> headersNew.put(key, singletonList(val)));
        headersNew.put("User-Agent",
                singletonList(UserAgentSupplier.getUserAgent()));
        return headersNew;
    }

    private void addIntuitTidIfAvailable(GraphQLRequest request,
                                         Map<String, List<String>> headersNew) {
        if (request.getTid() != null && !request.getTid().isEmpty()) {
            headersNew.put(HTTP_HEADER_TRACE_ID, singletonList(request.getTid()));
        }
    }

    private void addAuthorizationHeaderIfAvailable(GraphQLRequest request,
                                                   Map<String, List<String>> headersNew) {
        if (request.getAuthentication() != null) {
            String value = request.getAuthentication().getAuthorizationHeader();
            headersNew.put(HTTP_HEADER_AUTHORIZATION, singletonList(value));
        }
    }
}
