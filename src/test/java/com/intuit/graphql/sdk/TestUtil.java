package com.intuit.graphql.sdk;

import static java.nio.file.Files.readAllBytes;

import com.intuit.graphql.sdk.client.cache.SerializableGraphQLResponse;
import java.io.IOException;
import java.nio.file.Paths;
import org.springframework.util.SerializationUtils;

public class TestUtil {

  public static String readResourceFile(String resourceName) throws IOException {
    return new String(readAllBytes(Paths.get(
        TestUtil.class.getClassLoader()
            .getResource(resourceName).getPath())));
  }

  public static byte[] serialize(SerializableGraphQLResponse serializable) {
    return SerializationUtils.serialize(serializable);
  }

  public static Object deserialize(byte[] serialized) {
    return SerializationUtils.deserialize(serialized);
  }
}
