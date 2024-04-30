package com.intuit.graphql.sdk.client;

import com.intuit.graphql.sdk.client.auth.Authentication;

public class TestAuth implements Authentication {

  private final String userId;
  private final String token;

  public TestAuth(String userId, String token) {
    this.userId = userId;
    this.token = token;
  }

  @Override
  public String getAuthorizationHeader() {
    return userId + ":" + token;
  }
}
