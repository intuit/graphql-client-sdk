package com.intuit.graphql.sdk.client.util;


import com.intuit.graphql.sdk.client.util.LogEvent;
import com.intuit.graphql.sdk.client.util.LogEvent.LogEventName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LogEventTest {

  @Test
  void testLogEntry() {
    LogEvent event = new LogEvent();
    event.add("testEntry", "testEntryValue");
    assertEquals("testEntry=testEntryValue", event.getLogEntry());
  }

  @Test
  void testTimeKeepingInLogsEntry() {
    LogEvent event = new LogEvent();
    event.startTime();
    event.stopTime();
    assertTrue(event.getLogEntry().contains("time="));
  }

  @Test
  void testGetExecutionTimeWithNoTracking() {
    LogEvent event = new LogEvent();
    assertEquals(0, event.getExecutionTime());
  }

  @Test
  void testForConditionalLogging() {
    LogEvent event = new LogEvent(){
      @Override
      protected boolean shouldLog(LogEventName name) {
        return name == LogEventName.QUERY;
      }
    }; // Only query will be logged.

    event.add(LogEventName.QUERY, "query");
    event.add(LogEventName.HEADERS, "headers");
    event.add(LogEventName.OPERATION, "operation");
    event.add(LogEventName.URL, "url");
    event.add(LogEventName.TRANSACTION_ID, "tid");
    event.add(LogEventName.VARIABLES, "variables");
    event.add(LogEventName.RESPONSE, "response");
    assertEquals("query=query", event.getLogEntry()); // All other fields are ignored
  }

  @Test
  void testForConditionalLoggingAllAllowed() {
    LogEvent event = new LogEvent(){
      @Override
      protected boolean shouldLog(LogEventName name) {
        return true;
      }
    }; // Only query will be logged.

    event.add(LogEventName.QUERY, "query");
    event.add(LogEventName.OPERATION, "operation");
    event.add(LogEventName.URL, "url");
    event.add(LogEventName.TRANSACTION_ID, "tid");
    event.add(LogEventName.VARIABLES, "variables");
    event.add(LogEventName.RESPONSE, "response");
    event.add(LogEventName.TOTAL_REQUEST_COUNT, "totalRequestCount");
    event.add(LogEventName.IS_REQUEST_RETRIED, "isRequestRetried");
    assertEquals("query=query, operation=operation, url=url, tid=tid, variables=variables, response=response, totalRequestCount=totalRequestCount, isRequestRetried=isRequestRetried", event.getLogEntry());
  }

  @Test
  void testForDefaultLogging() {
    LogEvent event = new LogEvent();
    event.add(LogEventName.HEADERS, "headers");
    event.add(LogEventName.QUERY, "query");
    event.add(LogEventName.OPERATION, "operation");
    event.add(LogEventName.URL, "url");
    event.add(LogEventName.TRANSACTION_ID, "tid");
    event.add(LogEventName.VARIABLES, "variables");
    event.add(LogEventName.RESPONSE, "response");
    event.add(LogEventName.TOTAL_REQUEST_COUNT, "totalRequestCount");
    event.add(LogEventName.IS_REQUEST_RETRIED, "isRequestRetried");
    assertEquals("query=query, operation=operation, url=url, tid=tid, variables=variables, totalRequestCount=totalRequestCount, isRequestRetried=isRequestRetried", event.getLogEntry());
  }
}