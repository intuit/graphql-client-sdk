package com.intuit.graphql.sdk.client.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.intuit.graphql.sdk.client.util.Pair;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PairTest {

  @Test
  void testBuild() {
    Pair<String, String> pair = Pair.build("key", "value");
    assertEquals("key", pair.getKey());
    assertEquals("value", pair.getValue());
  }

  @Test
  void testBuildMapFor() {
    Pair<String, String> pair = Pair.build("key", "value");
    assertEquals("key", pair.getKey());
    assertEquals("value", pair.getValue());
    Map<String, String> map = Pair.buildMapFor(pair);
    assertEquals("value", map.get("key"));
  }
}
