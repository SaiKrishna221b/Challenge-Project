# Architecture & Design Decisions

This document outlines the specific architectural challenges faced during the development of the Sales Analysis tool and the design decisions implemented to ensure the application is performant, maintainable, and scalable.

## 1. Functional Paradigm & Immutability
**Challenge:** Traditional imperative programming with mutable state (loops, setters) is prone to side effects and harder to test in isolation.
**Decision:** strict adherence to **Functional Programming** principles, utilizing Java Streams and Immutable Data Models.
**Reasoning:** 
*   **Immutability:** Usage of Java 16+ `record` for the `Sale` model ensures that data cannot be modified after creation, eliminating a large class of bugs related to shared mutable state.
*   **Declarative Style:** Replacing explicit loops with Stream pipelines (`map`, `filter`, `reduce`) makes the code self-documenting. Instead of describing *how* to iterate, we describe *what* transformation to apply.

## 2. Memory Management (The "Big Data" Problem)
**Challenge:** Loading a massive CSV file (e.g., millions of rows) entirely into memory (`List<Sale>`) causes `OutOfMemoryError` on standard heaps.
**Decision:** Implementation of a **Lazy-Loading Chunk Processing Engine**.
**Reasoning:** The `processInChunks` method reads the file stream line-by-line and accumulates records into small, manageable batches (e.g., 1000 rows). Each batch is processed, aggregated into global statistics, and then discarded from memory. This architecture allows the application to process files of infinite size with a constant memory footprint (O(1) RAM usage).

## 3. Performance Optimization (Single-Pass Statistics)
**Challenge:** Calculating multiple metrics (Min, Max, Average, Sum) for a category usually requires iterating through the data multiple times or maintaining complex accumulator objects.
**Decision:** Utilization of `DoubleSummaryStatistics` and `Collectors.summarizingDouble`.
**Reasoning:** This specialized collector computes Count, Sum, Min, Max, and Average in a **single pass** over the data stream. This significantly reduces CPU cycles compared to iterating the list 5 separate times for 5 separate metrics.

## 4. Fault Tolerance
**Challenge:** In large datasets, a single malformed line or data corruption should not crash the entire analytics job.
**Decision:** Implementation of **Granular Error Handling & Retry Logic** at the batch level.
**Reasoning:** 
*   **Parsing:** Individual bad lines are caught and logged without stopping the stream.
*   **Processing:** The Chunk Processor includes a retry mechanism. If a batch fails to process (e.g., transient network or I/O glitch), it is retried up to 3 times before being marked as a failure. This ensures resilience in long-running jobs.

## 5. Type Safety & Precision
**Challenge:** Using `float` or `double` for monetary values can lead to floating-point rounding errors (e.g., `0.1 + 0.2 != 0.3`).
**Decision:** While `double` was used for this academic exercise for simplicity with Streams, the design acknowledges that a production financial system would use `BigDecimal`.
**Reasoning:** The current implementation uses `double` to fully leverage the specialized `DoubleStream` optimizations for high performance, accepting the trade-off of micro-precision for the sake of processing speed in this specific analytics context.

## 6. Environment consistency (Docker)
**Challenge:** Ensuring the Java application runs identically across different developer machines and CI environments.
**Decision:** Use of **Multi-Stage Docker Builds**.
**Reasoning:** 
*   **Stage 1 (Builder):** Uses a full JDK/Maven image to compile code and run tests.
*   **Stage 2 (Runtime):** Copies only the compiled classes to a lightweight JRE image.
This results in a significantly smaller final image (~200MB vs ~600MB) and ensures that the production container is free of build tools and source code, improving security and startup time.

