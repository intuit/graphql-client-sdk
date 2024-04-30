package com.intuit.graphql.sdk.client.auth;

import com.intuit.graphql.sdk.client.auth.Authentication;
import com.intuit.graphql.sdk.client.auth.ExistingAuthHeader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExistingAuthHeaderTest {

    @Test
    public void testExistingAuthHeader() {
        final Authentication authentication = new ExistingAuthHeader("Intuit_IAM_Authentication intuit_appid=test123");
        Assertions.assertEquals("Intuit_IAM_Authentication intuit_appid=test123", authentication.getAuthorizationHeader());
    }

}
