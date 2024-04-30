package com.intuit.graphql.sdk.client;

import com.intuit.graphql.sdk.client.cache.CacheBehavior;
import com.intuit.graphql.sdk.client.util.LogEvent;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PaginatedGraphQLRequestTest {
    public static final TestAuth AUTHENTICATION = new TestAuth("appId",
            "secret");
    public static final String TID = "tid";
    public static final String QUERY = "query";
    public static final String PAGE_INFO_JSON_PATH = "data.paginatedQuery.pageInfo";
    public static final String OPERATION = "operation";
    public static final String EXPECTED_AUTH_HEADER_VALUE = "appId:secret";

    public static final String END_CURSOR_FIELD_NAME = "END_CURSOR_FIELD_NAME";
    public static final String END_CURSOR = "END_CURSOR";

    public static final String PAGE_SIZE_FIELD_NAME = "PAGE_SIZE_FIELD_NAME";
    public static final int PAGE_SIZE = 10;

    @Test
    void testPaginatedGraphQLRequestContructionUsingBuilder() {
        PaginatedGraphQLRequest request = PaginatedGraphQLRequest.builder()
                .authentication(AUTHENTICATION)
                .tid(TID)
                .query(QUERY)
                .variables(new HashMap<>())
                .operation(OPERATION)
                .httpHeaders(Collections.emptyMap())
                .throwExceptionForErrorResponse(false)
                .logEvent(new LogEvent())
                .cacheBehavior(CacheBehavior.builder().build())
                .endCursorFieldName(END_CURSOR_FIELD_NAME)
                .endCursor(END_CURSOR)
                .pageSizeFieldName(PAGE_SIZE_FIELD_NAME)
                .pageSize(PAGE_SIZE)
                .pageInfoJsonPath(PAGE_INFO_JSON_PATH)
                .build();
        assertEquals(QUERY, request.getQuery());
        assertEquals(TID, request.getTid());
        assertEquals(EXPECTED_AUTH_HEADER_VALUE,
                request.getAuthentication().getAuthorizationHeader());
        assertEquals(OPERATION, request.getOperation());
        assertEquals(1, request.getVariables().size());
        assertEquals(END_CURSOR_FIELD_NAME, request.getEndCursorFieldName());
        assertEquals(END_CURSOR, request.getEndCursor());
        assertEquals(PAGE_SIZE_FIELD_NAME, request.getPageSizeFieldName());
        assertEquals(PAGE_SIZE, request.getPageSize());
    }

    @Test
    void testExceptionForMissingMandatoryArguments() {
        assertThrows(NullPointerException.class, () ->
                PaginatedGraphQLRequest.builder()
                        .tid(TID)
                        .authentication(AUTHENTICATION)
                        .build()
        );
    }

    @Test
    void testExceptionForMissingQueryName() {
        assertThrows(NullPointerException.class, () ->
                PaginatedGraphQLRequest.builder()
                        .tid(TID)
                        .authentication(AUTHENTICATION)
                        .operation(OPERATION)
                        .variables(Collections.emptyMap())
                        .httpHeaders(Collections.emptyMap())
                        .query(QUERY)
                        .endCursor(END_CURSOR)
                        .endCursorFieldName(END_CURSOR_FIELD_NAME)
                        .pageSize(10)
                        .pageSizeFieldName("pageSize")
                        .build()
        );
    }

    @Test
    void testDefaultValuesForPaginatedRequest() {
        PaginatedGraphQLRequest request = PaginatedGraphQLRequest.builder()
                .authentication(AUTHENTICATION)
                .tid(TID)
                .query(QUERY)
                .variables(Collections.emptyMap())
                .operation(OPERATION)
                .httpHeaders(Collections.emptyMap())
                .throwExceptionForErrorResponse(false)
                .logEvent(new LogEvent())
                .cacheBehavior(CacheBehavior.builder().build())
                .pageInfoJsonPath(PAGE_INFO_JSON_PATH)
                .build();
        assertEquals("after", request.getEndCursorFieldName());
        assertNull(request.getEndCursor());
        assertEquals("first", request.getPageSizeFieldName());
        assertEquals(10, request.getPageSize());
    }

    @Test
    void testGetNextPageRequest() {
        String oldEndCursor = "old_end_cursor";
        String newEndCursor = "new_end_cursor";
        Map<String, String> headers = getTestHeaders();
        Map<String, Object> startingVariables = getTestVariables();
        Map<String, Object> newVariables = getTestVariables();
        Map<String, Object> pagination = new HashMap<>();
        pagination.put(END_CURSOR_FIELD_NAME, newEndCursor);
        pagination.put(PAGE_SIZE_FIELD_NAME, PAGE_SIZE);
        newVariables.put("pagination", pagination);
        LogEvent logEvent = new LogEvent();
        CacheBehavior cacheBehavior = CacheBehavior.builder().build();
        PaginatedGraphQLRequest currentPageRequest = PaginatedGraphQLRequest.builder()
                .authentication(AUTHENTICATION)
                .tid(TID)
                .query(QUERY)
                .variables(startingVariables)
                .operation(OPERATION)
                .httpHeaders(headers)
                .throwExceptionForErrorResponse(false)
                .logEvent(logEvent)
                .cacheBehavior(cacheBehavior)
                .throwExceptionForErrorResponse(false)
                .pageInfoJsonPath(PAGE_INFO_JSON_PATH)
                .endCursorFieldName(END_CURSOR_FIELD_NAME)
                .pageSize(PAGE_SIZE)
                .pageSizeFieldName(PAGE_SIZE_FIELD_NAME)
                .endCursor(oldEndCursor)
                .build();
        PageInfo pageInfo = new PageInfo(true, newEndCursor);
        PaginatedGraphQLRequest result = currentPageRequest.getNextPageRequest(Optional.of(pageInfo)).get();
        assertEquals(AUTHENTICATION, result.getAuthentication());
        assertEquals(AUTHENTICATION, currentPageRequest.getAuthentication());
        assertEquals(TID, result.getTid());
        assertEquals(TID, currentPageRequest.getTid());
        assertEquals(QUERY, result.getQuery());
        assertEquals(QUERY, currentPageRequest.getQuery());
        assertEquals(newVariables, result.getVariables());
        assertEquals(startingVariables, currentPageRequest.getVariables());
        assertEquals(OPERATION, result.getOperation());
        assertEquals(OPERATION, currentPageRequest.getOperation());
        assertEquals(headers, result.getHttpHeaders());
        assertEquals(headers, currentPageRequest.getHttpHeaders());
        assertEquals(false, result.isThrowExceptionForErrorResponse());
        assertEquals(false, currentPageRequest.isThrowExceptionForErrorResponse());
        assertEquals(logEvent, result.getLogEvent());
        assertEquals(logEvent, currentPageRequest.getLogEvent());
        assertEquals(cacheBehavior, result.getCacheBehavior());
        assertEquals(cacheBehavior, currentPageRequest.getCacheBehavior());
        assertEquals(PAGE_INFO_JSON_PATH, result.getPageInfoJsonPath());
        assertEquals(PAGE_INFO_JSON_PATH, currentPageRequest.getPageInfoJsonPath());
        assertEquals(END_CURSOR_FIELD_NAME, result.getEndCursorFieldName());
        assertEquals(END_CURSOR_FIELD_NAME, currentPageRequest.getEndCursorFieldName());
        assertEquals(PAGE_SIZE, result.getPageSize());
        assertEquals(PAGE_SIZE, currentPageRequest.getPageSize());
        assertEquals(PAGE_SIZE_FIELD_NAME, result.getPageSizeFieldName());
        assertEquals(PAGE_SIZE_FIELD_NAME, currentPageRequest.getPageSizeFieldName());
        assertEquals(newEndCursor, result.getEndCursor());
        assertEquals(oldEndCursor, currentPageRequest.getEndCursor());
    }

    @Test
    public void testGetVariables() {
        Map<String, Object> startingVariables = getTestVariables();
        PaginatedGraphQLRequest request = PaginatedGraphQLRequest.builder()
                .authentication(AUTHENTICATION)
                .tid(TID)
                .query(QUERY)
                .variables(startingVariables)
                .operation(OPERATION)
                .httpHeaders(getTestHeaders())
                .throwExceptionForErrorResponse(false)
                .logEvent(new LogEvent())
                .cacheBehavior(CacheBehavior.builder().build())
                .throwExceptionForErrorResponse(false)
                .pageInfoJsonPath(PAGE_INFO_JSON_PATH)
                .endCursorFieldName(END_CURSOR_FIELD_NAME)
                .pageSize(PAGE_SIZE)
                .pageSizeFieldName(PAGE_SIZE_FIELD_NAME)
                .endCursor(END_CURSOR)
                .build();

        Map<String, Object> updatedVariables = request.getVariables();

        startingVariables.forEach((key, val) -> {
            assertTrue(updatedVariables.containsKey(key));
            assertEquals(val, updatedVariables.get(key));
        });

        assertTrue(updatedVariables.containsKey("pagination"));

        Map<String, Object> pagination = (Map<String, Object>) updatedVariables.get("pagination");
        assertEquals(END_CURSOR, pagination.get(END_CURSOR_FIELD_NAME));
        assertEquals(PAGE_SIZE, pagination.get(PAGE_SIZE_FIELD_NAME));
    }

    @Test
    public void testGetVariablesWhenEndCursorIsNotSet() {
        Map<String, Object> startingVariables = getTestVariables();
        PaginatedGraphQLRequest request = PaginatedGraphQLRequest.builder()
                .authentication(AUTHENTICATION)
                .tid(TID)
                .query(QUERY)
                .variables(startingVariables)
                .operation(OPERATION)
                .httpHeaders(getTestHeaders())
                .throwExceptionForErrorResponse(false)
                .logEvent(new LogEvent())
                .cacheBehavior(CacheBehavior.builder().build())
                .throwExceptionForErrorResponse(false)
                .pageInfoJsonPath(PAGE_INFO_JSON_PATH)
                .endCursorFieldName(END_CURSOR_FIELD_NAME)
                .pageSize(PAGE_SIZE)
                .pageSizeFieldName(PAGE_SIZE_FIELD_NAME)
                .build();

        Map<String, Object> updatedVariables = request.getVariables();

        startingVariables.forEach((key, val) -> {
            assertTrue(updatedVariables.containsKey(key));
            assertEquals(val, updatedVariables.get(key));
        });

        assertTrue(updatedVariables.containsKey("pagination"));

        Map<String, Object> pagination = (Map<String, Object>) updatedVariables.get("pagination");
        assertNull(pagination.get(END_CURSOR_FIELD_NAME));
        assertEquals(PAGE_SIZE, pagination.get(PAGE_SIZE_FIELD_NAME));
    }

    @Test
    public void testGetVariablesWhenEndCursorFieldNameIsNotSet() {
        Map<String, Object> startingVariables = getTestVariables();
        PaginatedGraphQLRequest request = PaginatedGraphQLRequest.builder()
                .authentication(AUTHENTICATION)
                .tid(TID)
                .query(QUERY)
                .variables(startingVariables)
                .operation(OPERATION)
                .httpHeaders(getTestHeaders())
                .throwExceptionForErrorResponse(false)
                .logEvent(new LogEvent())
                .cacheBehavior(CacheBehavior.builder().build())
                .throwExceptionForErrorResponse(false)
                .pageInfoJsonPath(PAGE_INFO_JSON_PATH)
                .endCursor(END_CURSOR)
                .pageSize(PAGE_SIZE)
                .pageSizeFieldName(PAGE_SIZE_FIELD_NAME)
                .build();

        Map<String, Object> updatedVariables = request.getVariables();

        startingVariables.forEach((key, val) -> {
            assertTrue(updatedVariables.containsKey(key));
            assertEquals(val, updatedVariables.get(key));
        });

        assertTrue(updatedVariables.containsKey("pagination"));

        Map<String, Object> pagination = (Map<String, Object>) updatedVariables.get("pagination");
        assertFalse(pagination.containsKey(END_CURSOR_FIELD_NAME));
        assertNull(pagination.get(END_CURSOR_FIELD_NAME));
        assertEquals(PAGE_SIZE, pagination.get(PAGE_SIZE_FIELD_NAME));
    }

    @Test
    public void testGetVariablesWhenPageSizeIsNotSet () {
        Map<String, Object> startingVariables = getTestVariables();
        PaginatedGraphQLRequest request = PaginatedGraphQLRequest.builder()
                .authentication(AUTHENTICATION)
                .tid(TID)
                .query(QUERY)
                .variables(startingVariables)
                .operation(OPERATION)
                .httpHeaders(getTestHeaders())
                .throwExceptionForErrorResponse(false)
                .logEvent(new LogEvent())
                .cacheBehavior(CacheBehavior.builder().build())
                .throwExceptionForErrorResponse(false)
                .pageInfoJsonPath(PAGE_INFO_JSON_PATH)
                .endCursorFieldName(END_CURSOR_FIELD_NAME)
                .endCursor(END_CURSOR)
                .pageSizeFieldName(PAGE_SIZE_FIELD_NAME)
                .build();

        Map<String, Object> updatedVariables = request.getVariables();

        startingVariables.forEach((key, val) -> {
            assertTrue(updatedVariables.containsKey(key));
            assertEquals(val, updatedVariables.get(key));
        });

        assertTrue(updatedVariables.containsKey("pagination"));

        Map<String, Object> pagination = (Map<String, Object>) updatedVariables.get("pagination");
        assertTrue(pagination.containsKey(END_CURSOR_FIELD_NAME));
        assertEquals(END_CURSOR, pagination.get(END_CURSOR_FIELD_NAME));
        assertEquals(10, pagination.get(PAGE_SIZE_FIELD_NAME));
    }

    @Test
    public void testGetVariablesWhenEndCursorAndPageSizeAreNotSet () {
        Map<String, Object> startingVariables = getTestVariables();
        PaginatedGraphQLRequest request = PaginatedGraphQLRequest.builder()
                .authentication(AUTHENTICATION)
                .tid(TID)
                .query(QUERY)
                .variables(startingVariables)
                .operation(OPERATION)
                .httpHeaders(getTestHeaders())
                .throwExceptionForErrorResponse(false)
                .logEvent(new LogEvent())
                .cacheBehavior(CacheBehavior.builder().build())
                .throwExceptionForErrorResponse(false)
                .pageInfoJsonPath(PAGE_INFO_JSON_PATH)
                .build();

        Map<String, Object> updatedVariables = request.getVariables();

        startingVariables.forEach((key, val) -> {
            assertTrue(updatedVariables.containsKey(key));
            assertEquals(val, updatedVariables.get(key));
        });

        assertTrue(updatedVariables.containsKey("pagination"));

        Map<String, Object> pagination = (Map<String, Object>) updatedVariables.get("pagination");
        assertFalse(pagination.containsKey(END_CURSOR_FIELD_NAME));
        assertEquals(null, pagination.get(END_CURSOR_FIELD_NAME));
        assertTrue(pagination.containsKey("first"));
        assertEquals(10, pagination.get("first"));
    }

    private Map<String, String> getTestHeaders() {
        HashMap<String, String> headersMap = new HashMap<>();
        headersMap.put("test-header", "test-value");
        return headersMap;
    }

    private Map<String, Object> getTestVariables() {
        HashMap<String, Object> variables = new HashMap<>();
        variables.put("test-variable", "test-value");
        return variables;
    }
}
