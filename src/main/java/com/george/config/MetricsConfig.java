package com.george.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Gauge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class MetricsConfig {

    private final AtomicInteger activeEmbeddingOperations = new AtomicInteger(0);
    private final AtomicInteger activeJobMatchingOperations = new AtomicInteger(0);

    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    @Bean
    public Counter embeddingGenerationCounter(MeterRegistry registry) {
        return Counter.builder("embeddings.generation.total")
                .description("Total number of embedding generations")
                .register(registry);
    }

    @Bean
    public Counter embeddingGenerationErrorsCounter(MeterRegistry registry) {
        return Counter.builder("embeddings.generation.errors")
                .description("Total number of embedding generation errors")
                .register(registry);
    }

    @Bean
    public Counter embeddingCacheHitsCounter(MeterRegistry registry) {
        return Counter.builder("embeddings.cache.hits")
                .description("Total number of embedding cache hits")
                .register(registry);
    }

    @Bean
    public Counter embeddingCacheMissesCounter(MeterRegistry registry) {
        return Counter.builder("embeddings.cache.misses")
                .description("Total number of embedding cache misses")
                .register(registry);
    }

    @Bean
    public Timer embeddingGenerationTimer(MeterRegistry registry) {
        return Timer.builder("embeddings.generation.duration")
                .description("Time taken to generate embeddings")
                .register(registry);
    }

    @Bean
    public Counter jobMatchingRequestsCounter(MeterRegistry registry) {
        return Counter.builder("job.matching.requests.total")
                .description("Total number of job matching requests")
                .register(registry);
    }

    @Bean
    public Counter jobMatchingErrorsCounter(MeterRegistry registry) {
        return Counter.builder("job.matching.errors")
                .description("Total number of job matching errors")
                .register(registry);
    }

    @Bean
    public Counter jobMatchesFoundCounter(MeterRegistry registry) {
        return Counter.builder("job.matching.matches.found")
                .description("Total number of job matches found")
                .tag("type", "total")
                .register(registry);
    }

    @Bean
    public Counter mongoOperationsCounter(MeterRegistry registry) {
        return Counter.builder("mongodb.operations.total")
                .description("Total number of MongoDB operations")
                .register(registry);
    }

    @Bean
    public Counter mongoOperationErrorsCounter(MeterRegistry registry) {
        return Counter.builder("mongodb.operations.errors")
                .description("Total number of MongoDB operation errors")
                .register(registry);
    }

    @Bean
    public Timer mongoOperationTimer(MeterRegistry registry) {
        return Timer.builder("mongodb.operations.duration")
                .description("Time taken for MongoDB operations")
                .register(registry);
    }

    @Bean
    public Gauge activeEmbeddingOperationsGauge(MeterRegistry registry) {
        return Gauge.builder("embeddings.operations.active", activeEmbeddingOperations, AtomicInteger::get)
                .description("Number of active embedding operations")
                .register(registry);
    }

    @Bean
    public Gauge activeJobMatchingOperationsGauge(MeterRegistry registry) {
        return Gauge.builder("job.matching.operations.active", activeJobMatchingOperations, AtomicInteger::get)
                .description("Number of active job matching operations")
                .register(registry);
    }

    public AtomicInteger getActiveEmbeddingOperations() {
        return activeEmbeddingOperations;
    }

    public AtomicInteger getActiveJobMatchingOperations() {
        return activeJobMatchingOperations;
    }
}

