package com.intuit.graphql.sdk.client;

import com.intuit.graphql.sdk.TestUtil;
import com.intuit.graphql.sdk.client.cache.CacheBehavior;
import com.intuit.graphql.sdk.client.util.LogEvent;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

public class PaginatedGraphQLResponseTest {

    @Test
    void testSetsPageInfoInConstructor() throws Exception {
        String paginatedResponseString = TestUtil.readResourceFile("sample-multi-page-response-1.json");
        PaginatedGraphQLRequest request = PaginatedGraphQLRequest.builder()
                .authentication(PaginatedGraphQLRequestTest.AUTHENTICATION)
                .tid(PaginatedGraphQLRequestTest.TID)
                .query(PaginatedGraphQLRequestTest.QUERY)
                .variables(new HashMap<>())
                .operation(PaginatedGraphQLRequestTest.OPERATION)
                .httpHeaders(new HashMap<>())
                .throwExceptionForErrorResponse(false)
                .logEvent(new LogEvent())
                .cacheBehavior(CacheBehavior.builder().build())
                .endCursorFieldName(PaginatedGraphQLRequestTest.END_CURSOR_FIELD_NAME)
                .endCursor(PaginatedGraphQLRequestTest.END_CURSOR)
                .pageSizeFieldName(PaginatedGraphQLRequestTest.PAGE_SIZE_FIELD_NAME)
                .pageSize(PaginatedGraphQLRequestTest.PAGE_SIZE)
                .pageInfoJsonPath(PaginatedGraphQLRequestTest.PAGE_INFO_JSON_PATH)
                .build();

        GraphQLResponse response = new GraphQLResponse(paginatedResponseString);
        PaginatedGraphQLResponse paginatedResponse = new PaginatedGraphQLResponse(request, response);
        Assertions.assertTrue(paginatedResponse.hasNextPage());
        Assertions.assertTrue(paginatedResponse.getNextCursor().isPresent());
        Assertions.assertEquals("endCursor-1", paginatedResponse.getNextCursor().get());
    }

    @Test
    void testSetsPageInfoToEmptyOptionalWhenResponseDoesNotHavePageInfoFields() throws IOException {
        String responseString = TestUtil.readResourceFile("sample-response.json");
        PaginatedGraphQLRequest request = PaginatedGraphQLRequest.builder()
                .authentication(PaginatedGraphQLRequestTest.AUTHENTICATION)
                .tid(PaginatedGraphQLRequestTest.TID)
                .query(PaginatedGraphQLRequestTest.QUERY)
                .variables(Collections.emptyMap())
                .operation(PaginatedGraphQLRequestTest.OPERATION)
                .httpHeaders(Collections.emptyMap())
                .throwExceptionForErrorResponse(false)
                .logEvent(new LogEvent())
                .cacheBehavior(CacheBehavior.builder().build())
                .endCursorFieldName(PaginatedGraphQLRequestTest.END_CURSOR_FIELD_NAME)
                .endCursor(PaginatedGraphQLRequestTest.END_CURSOR)
                .pageSizeFieldName(PaginatedGraphQLRequestTest.PAGE_SIZE_FIELD_NAME)
                .pageSize(PaginatedGraphQLRequestTest.PAGE_SIZE)
                .pageInfoJsonPath(PaginatedGraphQLRequestTest.PAGE_INFO_JSON_PATH)
                .build();
        GraphQLResponse response = new GraphQLResponse(responseString);
        PaginatedGraphQLResponse paginatedGraphQLResponse = new PaginatedGraphQLResponse(request, response);
        Assertions.assertFalse(paginatedGraphQLResponse.getPageInfo().isPresent());
        Assertions.assertFalse(paginatedGraphQLResponse.hasNextPage());
        Assertions.assertFalse(paginatedGraphQLResponse.getNextPageRequest().isPresent());
    }

    @Test
    void testSets_PageInfoHasNextPage_ToFalse_WhenResponseHas_MissingField_HasNextPage() throws IOException {
        String responseString = TestUtil.readResourceFile("sample-invalid-paginated-response-1.json");
        PaginatedGraphQLRequest request = PaginatedGraphQLRequest.builder()
                .authentication(PaginatedGraphQLRequestTest.AUTHENTICATION)
                .tid(PaginatedGraphQLRequestTest.TID)
                .query(PaginatedGraphQLRequestTest.QUERY)
                .variables(Collections.emptyMap())
                .operation(PaginatedGraphQLRequestTest.OPERATION)
                .httpHeaders(Collections.emptyMap())
                .throwExceptionForErrorResponse(false)
                .logEvent(new LogEvent())
                .cacheBehavior(CacheBehavior.builder().build())
                .endCursorFieldName(PaginatedGraphQLRequestTest.END_CURSOR_FIELD_NAME)
                .endCursor(PaginatedGraphQLRequestTest.END_CURSOR)
                .pageSizeFieldName(PaginatedGraphQLRequestTest.PAGE_SIZE_FIELD_NAME)
                .pageSize(PaginatedGraphQLRequestTest.PAGE_SIZE)
                .pageInfoJsonPath(PaginatedGraphQLRequestTest.PAGE_INFO_JSON_PATH)
                .build();
        GraphQLResponse response = new GraphQLResponse(responseString);
        PaginatedGraphQLResponse paginatedGraphQLResponse = new PaginatedGraphQLResponse(request, response);
        Assertions.assertTrue(paginatedGraphQLResponse.getPageInfo().isPresent());
        Assertions.assertFalse(paginatedGraphQLResponse.getPageInfo().get().isHasNextPage());
        Assertions.assertFalse(paginatedGraphQLResponse.hasNextPage());
        Assertions.assertFalse(paginatedGraphQLResponse.getNextPageRequest().isPresent());
    }

    @Test
    void testSetsPageInfoToEmptyOptionalWhenResponseHasMissingField_EndCursor() throws IOException {
        String responseString = TestUtil.readResourceFile("sample-invalid-paginated-response-2.json");
        PaginatedGraphQLRequest request = PaginatedGraphQLRequest.builder()
                .authentication(PaginatedGraphQLRequestTest.AUTHENTICATION)
                .tid(PaginatedGraphQLRequestTest.TID)
                .query(PaginatedGraphQLRequestTest.QUERY)
                .variables(Collections.emptyMap())
                .operation(PaginatedGraphQLRequestTest.OPERATION)
                .httpHeaders(Collections.emptyMap())
                .throwExceptionForErrorResponse(false)
                .logEvent(new LogEvent())
                .cacheBehavior(CacheBehavior.builder().build())
                .endCursorFieldName(PaginatedGraphQLRequestTest.END_CURSOR_FIELD_NAME)
                .endCursor(PaginatedGraphQLRequestTest.END_CURSOR)
                .pageSizeFieldName(PaginatedGraphQLRequestTest.PAGE_SIZE_FIELD_NAME)
                .pageSize(PaginatedGraphQLRequestTest.PAGE_SIZE)
                .pageInfoJsonPath(PaginatedGraphQLRequestTest.PAGE_INFO_JSON_PATH)
                .build();
        GraphQLResponse response = new GraphQLResponse(responseString);
        PaginatedGraphQLResponse paginatedGraphQLResponse = new PaginatedGraphQLResponse(request, response);
        Assertions.assertFalse(paginatedGraphQLResponse.getPageInfo().isPresent());
        Assertions.assertFalse(paginatedGraphQLResponse.hasNextPage());
        Assertions.assertFalse(paginatedGraphQLResponse.getNextPageRequest().isPresent());
    }

    @Test
    void testSetsPageInfoToEmptyOptionalWhenResponseHasInvalidPageInfoFields() throws IOException {
        String responseString = TestUtil.readResourceFile("sample-invalid-paginated-response-3.json");
        PaginatedGraphQLRequest request = PaginatedGraphQLRequest.builder()
                .authentication(PaginatedGraphQLRequestTest.AUTHENTICATION)
                .tid(PaginatedGraphQLRequestTest.TID)
                .query(PaginatedGraphQLRequestTest.QUERY)
                .variables(Collections.emptyMap())
                .operation(PaginatedGraphQLRequestTest.OPERATION)
                .httpHeaders(Collections.emptyMap())
                .throwExceptionForErrorResponse(false)
                .logEvent(new LogEvent())
                .cacheBehavior(CacheBehavior.builder().build())
                .endCursorFieldName(PaginatedGraphQLRequestTest.END_CURSOR_FIELD_NAME)
                .endCursor(PaginatedGraphQLRequestTest.END_CURSOR)
                .pageSizeFieldName(PaginatedGraphQLRequestTest.PAGE_SIZE_FIELD_NAME)
                .pageSize(PaginatedGraphQLRequestTest.PAGE_SIZE)
                .pageInfoJsonPath(PaginatedGraphQLRequestTest.PAGE_INFO_JSON_PATH)
                .build();
        GraphQLResponse response = new GraphQLResponse(responseString);
        PaginatedGraphQLResponse paginatedGraphQLResponse = new PaginatedGraphQLResponse(request, response);
        Assertions.assertFalse(paginatedGraphQLResponse.getPageInfo().isPresent());
        Assertions.assertFalse(paginatedGraphQLResponse.hasNextPage());
        Assertions.assertFalse(paginatedGraphQLResponse.getNextPageRequest().isPresent());
    }

    @Test
    void testSetsPageInfoToEmptyOptionalWhenPageInfoJsonPathIsInvalid() throws IOException {
        String responseString = TestUtil.readResourceFile("sample-paginated-response.json");
        PaginatedGraphQLRequest request = PaginatedGraphQLRequest.builder()
                .authentication(PaginatedGraphQLRequestTest.AUTHENTICATION)
                .tid(PaginatedGraphQLRequestTest.TID)
                .query(PaginatedGraphQLRequestTest.QUERY)
                .variables(Collections.emptyMap())
                .operation(PaginatedGraphQLRequestTest.OPERATION)
                .httpHeaders(Collections.emptyMap())
                .throwExceptionForErrorResponse(false)
                .logEvent(new LogEvent())
                .cacheBehavior(CacheBehavior.builder().build())
                .endCursorFieldName(PaginatedGraphQLRequestTest.END_CURSOR_FIELD_NAME)
                .endCursor(PaginatedGraphQLRequestTest.END_CURSOR)
                .pageSizeFieldName(PaginatedGraphQLRequestTest.PAGE_SIZE_FIELD_NAME)
                .pageSize(PaginatedGraphQLRequestTest.PAGE_SIZE)
                .pageInfoJsonPath("data.iDoNotExist.pageInfo")
                .build();
        GraphQLResponse response = new GraphQLResponse(responseString);
        PaginatedGraphQLResponse paginatedGraphQLResponse = new PaginatedGraphQLResponse(request, response);
        Assertions.assertFalse(paginatedGraphQLResponse.getPageInfo().isPresent());
        Assertions.assertFalse(paginatedGraphQLResponse.hasNextPage());
        Assertions.assertFalse(paginatedGraphQLResponse.getNextPageRequest().isPresent());
    }

    @Test
    void testHasNextPageMethodForValidResponseWithoutNextPage() throws IOException {
        String responseString = TestUtil.readResourceFile("sample-paginated-response.json");
        PaginatedGraphQLRequest request = PaginatedGraphQLRequest.builder()
                .authentication(PaginatedGraphQLRequestTest.AUTHENTICATION)
                .tid(PaginatedGraphQLRequestTest.TID)
                .query(PaginatedGraphQLRequestTest.QUERY)
                .variables(Collections.emptyMap())
                .operation(PaginatedGraphQLRequestTest.OPERATION)
                .httpHeaders(Collections.emptyMap())
                .throwExceptionForErrorResponse(false)
                .logEvent(new LogEvent())
                .cacheBehavior(CacheBehavior.builder().build())
                .endCursorFieldName(PaginatedGraphQLRequestTest.END_CURSOR_FIELD_NAME)
                .endCursor(PaginatedGraphQLRequestTest.END_CURSOR)
                .pageSizeFieldName(PaginatedGraphQLRequestTest.PAGE_SIZE_FIELD_NAME)
                .pageSize(PaginatedGraphQLRequestTest.PAGE_SIZE)
                .pageInfoJsonPath(PaginatedGraphQLRequestTest.PAGE_INFO_JSON_PATH)
                .build();
        GraphQLResponse response = new GraphQLResponse(responseString);
        PaginatedGraphQLResponse paginatedGraphQLResponse = new PaginatedGraphQLResponse(request, response);
        Assertions.assertFalse(paginatedGraphQLResponse.hasNextPage());
    }

    @Test
    void testHasNextPageMethodForValidResponseWithNextPage() throws IOException {
        String responseString = TestUtil.readResourceFile("sample-multi-page-response-1.json");
        PaginatedGraphQLRequest request = PaginatedGraphQLRequest.builder()
                .authentication(PaginatedGraphQLRequestTest.AUTHENTICATION)
                .tid(PaginatedGraphQLRequestTest.TID)
                .query(PaginatedGraphQLRequestTest.QUERY)
                .variables(Collections.emptyMap())
                .operation(PaginatedGraphQLRequestTest.OPERATION)
                .httpHeaders(Collections.emptyMap())
                .throwExceptionForErrorResponse(false)
                .logEvent(new LogEvent())
                .cacheBehavior(CacheBehavior.builder().build())
                .endCursorFieldName(PaginatedGraphQLRequestTest.END_CURSOR_FIELD_NAME)
                .endCursor(PaginatedGraphQLRequestTest.END_CURSOR)
                .pageSizeFieldName(PaginatedGraphQLRequestTest.PAGE_SIZE_FIELD_NAME)
                .pageSize(PaginatedGraphQLRequestTest.PAGE_SIZE)
                .pageInfoJsonPath(PaginatedGraphQLRequestTest.PAGE_INFO_JSON_PATH)
                .build();
        GraphQLResponse response = new GraphQLResponse(responseString);
        PaginatedGraphQLResponse paginatedGraphQLResponse = new PaginatedGraphQLResponse(request, response);
        Assertions.assertTrue(paginatedGraphQLResponse.hasNextPage());
    }
}
