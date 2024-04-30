package com.intuit.graphql.sdk.client;

import static com.intuit.graphql.sdk.client.util.LogEvent.LogEventName.IS_REQUEST_RETRIED;
import static com.intuit.graphql.sdk.client.util.LogEvent.LogEventName.TOTAL_REQUEST_COUNT;

import com.intuit.graphql.sdk.client.config.GraphQLClientConfiguration;
import com.intuit.graphql.sdk.client.exceptions.GraphQLException;
import com.intuit.graphql.sdk.client.resiliency.CircuitBreakerRegister;
import com.intuit.graphql.sdk.client.resiliency.CircuitOpenHandler;
import com.intuit.graphql.sdk.client.resiliency.RetryRegister;
import com.netflix.graphql.dgs.client.GraphQLResponse;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class ResiliencyRequestHandler implements RequestHandler {

    private final CircuitBreaker circuitBreaker;
    private final CircuitOpenHandler circuitOpenHandler;
    private final Retry retry;

    private final RequestExecutor requestExecutor;

    public ResiliencyRequestHandler(GraphQLClientConfiguration configuration,
                                    RequestExecutor requestExecutor) {
        this.requestExecutor = requestExecutor;
        this.circuitBreaker = new CircuitBreakerRegister(
                configuration.getResilienceConfiguration()
                        .getCircuitBreakerConfig(),
                configuration.getGraphQLApiUrl()).getCircuitBreaker();
        this.retry = new RetryRegister(configuration.getResilienceConfiguration()
                .getRetryConfig(), configuration.getGraphQLApiUrl()).getRetry();
        this.circuitOpenHandler = configuration.getResilienceConfiguration()
                .getCircuitBreakerConfig().getCircuitOpenHandler();
    }

    public GraphQLResponse execute(GraphQLRequest graphQLRequest)
            throws GraphQLException {
        AtomicInteger retryCount = new AtomicInteger(0);
        Callable<GraphQLResponse> callable = () -> {
            retryCount.getAndIncrement();
            return requestExecutor.executeRequest(graphQLRequest);
        };
        callable = CircuitBreaker.decorateCallable(circuitBreaker, callable);
        callable = Retry.decorateCallable(retry, callable);
        try {
            return callable.call();
        } catch (CallNotPermittedException e) {
            return circuitOpenHandler.handleCallNotPermittedException(
                    graphQLRequest, e);
        } catch (GraphQLException e) {
            throw e;
        } catch (Exception e) {
            throw new GraphQLException(e);
        } finally {
            graphQLRequest.addToLogEvent(
                    IS_REQUEST_RETRIED, retryCount.get() > 1);
            graphQLRequest.addToLogEvent(TOTAL_REQUEST_COUNT, retryCount);
        }
    }

}
