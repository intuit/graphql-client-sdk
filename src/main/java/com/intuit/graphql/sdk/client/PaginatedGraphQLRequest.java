package com.intuit.graphql.sdk.client;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Getter
@SuperBuilder
public class PaginatedGraphQLRequest extends GraphQLRequest {
    @Builder.Default
    private final String endCursorFieldName = "after";

    private String endCursor;

    @Builder.Default
    private final String pageSizeFieldName = "first";

    @Builder.Default
    private final int pageSize = 10;

    @NonNull
    private final String pageInfoJsonPath;

    @Override
    public Map<String, Object> getVariables() {
        Map<String, Object> variables = super.getVariables() == null ? new HashMap<>() : super.getVariables();

        Map<String, Object> pagination = new HashMap<>();
        if (this.endCursorFieldName != null && this.endCursor != null) {
            pagination.put(this.endCursorFieldName, this.endCursor);
        }
        pagination.put(this.pageSizeFieldName, this.pageSize);
        variables.put("pagination", pagination);
        return variables;
    }

    public Optional<PaginatedGraphQLRequest> getNextPageRequest(Optional<PageInfo> pageInfo) {
        if (!pageInfo.isPresent() ||
                !pageInfo.get().isHasNextPage() ||
                (pageInfo.get().isHasNextPage() && pageInfo.get().getEndCursor() == null)) {
            return Optional.empty();
        }

        return Optional.of(PaginatedGraphQLRequest.builder()
                .authentication(this.getAuthentication())
                .tid(this.getTid())
                .query(this.getQuery())
                .variables(super.getVariables() == null ? new HashMap<>() : new HashMap<>(super.getVariables()))
                .operation(this.getOperation())
                .httpHeaders(this.getHttpHeaders())
                .logEvent(this.getLogEvent())
                .cacheBehavior(this.getCacheBehavior())
                .throwExceptionForErrorResponse(this.isThrowExceptionForErrorResponse())
                .pageInfoJsonPath(this.getPageInfoJsonPath())
                .endCursorFieldName(this.getEndCursorFieldName())
                .pageSize(this.getPageSize())
                .pageSizeFieldName(this.getPageSizeFieldName())
                .endCursor(pageInfo.get().getEndCursor())
                .build());
    }
}
