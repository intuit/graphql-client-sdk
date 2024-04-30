package com.intuit.graphql.sdk.client.util;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

public class LogEvent {

  public static final String EQUALS = "=";
  public static final String TIME = "time";
  protected final Map<String, Object> logEntries;
  protected long startTime;
  protected long endTime;

  public LogEvent() {
    this.logEntries = new LinkedHashMap<>();
  }

  public LogEvent add(String name, Object value) {
    logEntries.put(name, value);
    return this;
  }

  public LogEvent add(LogEventName name, Object value) {
    if(shouldLog(name)) {
      logEntries.put(name.getValue(), value);
    }
    return this;
  }

  public void startTime() {
    this.startTime = Instant.now().toEpochMilli();
  }

  public void stopTime() {
    this.endTime = Instant.now().toEpochMilli();
  }

  public long getExecutionTime() {
    if (isTrackingTime()) {
      return endTime - startTime;
    }
    return 0;
  }

  protected boolean isTrackingTime() {
    return startTime > 0 && endTime > 0;
  }

  public String getLogEntry() {
    StringJoiner stringJoiner = new StringJoiner(", ");
    if (isTrackingTime()) {
      logEntries.put(TIME, getExecutionTime());
    }
    logEntries.forEach((k, v) -> stringJoiner.add(k + EQUALS + v));
    return stringJoiner.toString();
  }

  /**
   * Override this method to control which log events should be logged.
   * @param logEventName name of the log event
   * @return true if the log event should be logged, false otherwise.
   */
  protected boolean shouldLog(LogEventName logEventName) {
    return !(logEventName == LogEventName.HEADERS || logEventName == LogEventName.RESPONSE);
  }

  public enum LogEventName {
    RESPONSE("response"),
    URL("url"),
    TRANSACTION_ID("tid"),
    VARIABLES("variables"),
    OPERATION("operation"),
    QUERY("query"),
    IS_REQUEST_RETRIED("isRequestRetried"),
    TOTAL_REQUEST_COUNT("totalRequestCount"),
    CACHE_STATUS("cacheStatus"),
    HEADERS("headers"),

    END_CURSOR_FIELD_NAME("endCursorFieldName"),
    END_CURSOR("endCursor"),
    PAGE_SIZE_FIELD_NAME("pageSizeFieldName"),
    PAGE_SIZE("pageSize")
    ;

    private final String value;

    LogEventName(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }
}