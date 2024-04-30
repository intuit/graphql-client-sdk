package com.intuit.graphql.sdk.client.auth;

public class ExistingAuthHeader implements Authentication {

    private final String header;

    public ExistingAuthHeader(String header) {
        this.header = header;
    }

    @Override
    public String getAuthorizationHeader() {
        return header;
    }

}
