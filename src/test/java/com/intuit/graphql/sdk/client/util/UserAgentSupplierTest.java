package com.intuit.graphql.sdk.client.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.intuit.graphql.sdk.client.util.UserAgentSupplier;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

class UserAgentSupplierTest {

  @Test
  void getUserAgent() {
    String userAgent = UserAgentSupplier.getUserAgent();
    assertEquals("com.intuit.graphql-sdk/unknown", userAgent);
    // It will return version as unknown in the test cases.
    // During runtime, it will read the version from pom.properties.
  }

  @Test
  void testExtractVersion() {
    Optional<String> version = UserAgentSupplier.extractVersion(Logger.class);
    assertEquals("2.0.5", version.orElse(
        "unknown"), "Update the expected version if the version of slf4j-api changes and this test case fails.");
  }
}