package com.intuit.graphql.sdk.client;

import com.intuit.graphql.sdk.client.auth.Authentication;
import com.intuit.graphql.sdk.client.cache.CacheBehavior;
import com.intuit.graphql.sdk.client.util.LogEvent;
import com.intuit.graphql.sdk.client.util.LogEvent.LogEventName;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class GraphQLRequest {

    private final Authentication authentication;
    private final String tid;
    @NonNull
    private final String query;
    private final Map<String, Object> variables;
    private final String operation;
    @Builder.Default
    private final Map<String, String> httpHeaders = new HashMap<>();
    @Builder.Default
    private boolean throwExceptionForErrorResponse = true; // The code analyzer is flagging it if we make it a final field. It will suggest to make it static which is not what we want to do with it.
    @Builder.Default
    private final LogEvent logEvent = new LogEvent();

    /**
     * Setting this field to true would remove null fields from the input variables.
     */
    @Builder.Default
    private boolean omitNullFieldsFromInput = false;

    // The cache is disabled by default
    @Builder.Default
    private final CacheBehavior cacheBehavior = CacheBehavior.builder()
        .isCacheEnabled(false).build();

    public void addToLogEvent(LogEventName key, Object value) {
        logEvent.add(key, value);
    }

}
