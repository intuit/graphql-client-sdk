package com.intuit.graphql.sdk.client.util;

import static java.lang.String.format;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class UserAgentSupplier {

  private static final String USER_AGENT_NAME = "com.intuit.graphql-sdk";
  private static final String VERSION = extractVersion(UserAgentSupplier.class)
      .orElse("unknown");
  private static final String USER_AGENT = format("%s/%s", USER_AGENT_NAME,
      VERSION);

  private UserAgentSupplier() {
  }

  public static String getUserAgent() {
    return USER_AGENT;
  }

  // The following code is referenced from :
  // https://stackoverflow.com/questions/49889510/can-i-determine-the-version-of-a-java-library-at-runtime

  /**
   * Reads a library's version if the library contains a Maven pom.properties
   * file. You probably want to cache the output or write it to a constant.
   *
   * @param referenceClass any class from the library to check
   * @return an Optional containing the version String, if present
   */
  static Optional<String> extractVersion(
      final Class<?> referenceClass) {
    return Optional.ofNullable(referenceClass)
        .map(cls -> unthrow(cls::getProtectionDomain))
        .map(java.security.ProtectionDomain::getCodeSource)
        .map(java.security.CodeSource::getLocation)
        .map(url -> unthrow(url::openStream))
        .map(is -> unthrow(() -> new JarInputStream(is)))
        .map(jis -> readPomProperties(jis, referenceClass))
        .map(props -> props.getProperty("version"));
  }

  /**
   * Locate the pom.properties file in the Jar, if present, and return a
   * Properties object representing the properties in that file.
   *
   * @param jarInputStream the jar stream to read from
   * @param referenceClass the reference class, whose ClassLoader we'll be
   *                       using
   * @return the Properties object, if present, otherwise null
   */
  private static Properties readPomProperties(
      final JarInputStream jarInputStream,
      final Class<?> referenceClass) {

    try {
      JarEntry jarEntry;
      while ((jarEntry = jarInputStream.getNextJarEntry()) != null) {
        String entryName = jarEntry.getName();
        if (entryName.startsWith("META-INF")
            && entryName.endsWith("pom.properties")) {

          Properties properties = new Properties();
          ClassLoader classLoader = referenceClass.getClassLoader();
          properties.load(classLoader.getResourceAsStream(entryName));
          return properties;
        }
      }
    } catch (IOException ignored) {
      //Ignoring it
    }
    return new Properties();
  }

  /**
   * Wrap a Callable with code that returns null when an exception occurs, so
   * it can be used in an Optional.map() chain.
   */
  private static <T> T unthrow(final Callable<T> code) {
    try {
      return code.call();
    } catch (Exception ignored) {
      return null;
    }
  }
}
