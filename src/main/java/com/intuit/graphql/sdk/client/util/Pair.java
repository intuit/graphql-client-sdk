package com.intuit.graphql.sdk.client.util;

import java.util.HashMap;
import java.util.Map;

public class Pair<K, V> {

  private final K key;
  private final V value;

  public Pair(K key, V value) {
    this.key = key;
    this.value = value;
  }

  public static <K, V> Pair<K, V> build(K key, V value) {
    return new Pair<>(key, value);
  }

  public K getKey() {
    return key;
  }

  public V getValue() {
    return value;
  }

  @SafeVarargs
  public static <K, V> Map<K, V> buildMapFor(Pair<K, V>... stringStringPair) {
    Map<K, V> map = new HashMap<>();
    for (Pair<K, V> pair : stringStringPair) {
      map.put(pair.getKey(), pair.getValue());
    }
    return map;
  }
}
