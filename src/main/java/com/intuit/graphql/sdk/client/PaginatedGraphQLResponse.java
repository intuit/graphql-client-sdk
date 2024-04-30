package com.intuit.graphql.sdk.client;

import com.intuit.graphql.sdk.client.exceptions.GraphQLException;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Setter
@Getter
public class PaginatedGraphQLResponse {
    @Getter
    private GraphQLResponse response;
    private Optional<PaginatedGraphQLRequest> nextPageRequest;
    private Optional<PageInfo> pageInfo;

    public PaginatedGraphQLResponse(PaginatedGraphQLRequest currentPageRequest, GraphQLResponse response) {
        this.response = response;
        try {
            PageInfo pageInfo = response.extractValueAsObject(
                    currentPageRequest.getPageInfoJsonPath(),
                    PageInfo.class
            );
            if (pageInfo.isHasNextPage() && pageInfo.getEndCursor() == null) {
                this.pageInfo = Optional.empty();
            } else {
                this.pageInfo = Optional.of(pageInfo);
            }
            this.nextPageRequest = currentPageRequest.getNextPageRequest(this.pageInfo);
        } catch (Exception e) {
            this.pageInfo = Optional.empty();
            this.nextPageRequest = Optional.empty();
        }
    }

    public boolean hasNextPage() {
        return pageInfo.isPresent() &&
                nextPageRequest.isPresent() &&
                pageInfo.get().isHasNextPage() &&
                pageInfo.get().getEndCursor() != null;
    }

    public Optional<String> getNextCursor() {
        return pageInfo.map(PageInfo::getEndCursor);
    }

    public Optional<PaginatedGraphQLResponse> getNextPageResponse(
        GraphQLClient client)
            throws GraphQLException {
        if (this.hasNextPage()) {
            PaginatedGraphQLRequest request = this.getNextPageRequest().get();
            return Optional.of(client.execute(request));
        }

        return Optional.empty();
    }
}
