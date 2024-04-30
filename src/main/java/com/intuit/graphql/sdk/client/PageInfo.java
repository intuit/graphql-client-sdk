package com.intuit.graphql.sdk.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageInfo {
    @Builder.Default
    private boolean hasNextPage = false;
    private String endCursor;
}
