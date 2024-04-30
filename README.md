# Intuit GraphQL SDK

This is a Java SDK for consuming GraphQL API's. This client is written over [DGS Java Client](https://netflix.github.io/dgs/advanced/java-client/) with following requirements in mind:


- Simple consistent ways to build GraphQL Queries
- Consistent way of error handling
- Resiliency (Retries/Circuit Breaker)
- Client side caching 
- Logging best practices (e.g. metrics collection)
- Developer Productivity 
  - Teams should be able to consume this library for new queries within a couple of hours 
  - A new service should be able to consume this SDK within a day
  - New queries should not modify SDK
  - GraphQLs can have multiple queries for the same entity. For example a team can build queries with different fragments based on their use case 
- Need to support Java 8 (the lowest version)
- Minimal dependencies to avoid conflicts.


## Build

To build and run the unit tests:

```bash
mvn clean install
```

## Usage

Add it as a dependency to your project (Get the version from [here](https://github.com/intuit/graphql-client-sdk/releases)) 


```xml
<dependency>
  <groupId>com.intuit</groupId>
  <artifactId>graphql-sdk</artifactId>
  <version>{graphql-sdk.version}</version>
</dependency>
```

To consume any GraphQL API, you need to provide the following:

* A GraphQL endpoint URL
* A GraphQL query
* A GraphQL operation name (optional)
* GraphQL variables (optional) - if they are referred in the query

Check the following code to understand how to make a GraphQL request:

```java
import com.intuit.graphql.sdk.client.auth.Authentication;

public class Sample {

  public static void main(String[] args) {
    // Initialize the graphql client. 
    // Use this same instance for future calls to use http connection pooling.
   GraphQLClient graphQLClient = new GraphQLClient(
            GraphQLConfiguration.builder()
                    .graphQLApiUrl("https://www.example.org/graphql")
                    .httpConfiguration(HttpConfiguration.builder().build())
                    .resilienceConfiguration(
                            ResiliencyConfiguration.builder()
                                    .retryConfig(RetryConfiguration.builder()
                                            .build())
                                    .circuitBreakerConfig(
                                            CircuitBreakerConfiguration.builder()
                                                    .build()).build())
                    .build()
    );
    // While building GraphQLClient object if any required field is missing, 
    // it will throw a NullPointerException. 
    // Most of the fields have their default value set. 
    // So you can skip them if you want to use the default value. 
    // The recommendation is to externalize all configurable options 
    // so that they can be changed without code change.

    String sampleQuery = "query example..."; // Use the query from the GraphQL playground
    Map<String, Object> variablesMap = new HashMap<>(); // Pass the variables here

    GraphQLResponse lookUpAccountByIdResponse = graphQLClient
            .execute(GraphQLRequest.builder()
                    .query(sampleQuery) // Mandatory
                    .variables(variablesMap) // Optional 
                    .tid(UUID.randomUUID().toString()) // Optional
                    .authentication(new CustomAuthentication(userId, token)) // Create an authentication provider class as per your usage requirement or pass the authorization header directly using httpHeaders method
                    .httpHeaders(
                            getCustomHeaders()) // We can pass any header using this method.
                    .build());

  }

}

```