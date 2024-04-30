package com.intuit.graphql.sdk.client;

import com.intuit.graphql.sdk.client.PageInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PageInfoTest {

    @Test
    public void testPageInfoCreationByBuilder() {
        PageInfo result = PageInfo.builder()
                .endCursor("endCursor")
                .hasNextPage(true)
                .build();
        Assertions.assertTrue(result.isHasNextPage());
        Assertions.assertEquals("endCursor", result.getEndCursor());
    }

    @Test
    public void testPageInfoDefaultValues() {
        PageInfo result = PageInfo.builder().build();
        Assertions.assertFalse(result.isHasNextPage());
        Assertions.assertNull(result.getEndCursor());
    }
}
