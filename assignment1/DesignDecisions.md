# Architecture & Design Decisions

This document outlines the specific architectural challenges faced during development and the design decisions implemented to ensure the application is robust, verifiable, and scalable.

## 1. Core Concurrency Model
**Challenge:** Managing race conditions without introducing bugs with manual lock management.
**Decision:** Utilization of Python's standard `queue.Queue` for the shared buffer.
**Reasoning:** While manual `threading.Condition` and `Lock` implementation was considered, `queue.Queue` provides a battle-tested, thread-safe implementation of the Bounded Buffer pattern that internally handles all locking and signaling (`wait`/`notify`) correctly. This choice prioritizes system stability and code readability over the complexity of re-implementing low-level synchronization primitives, ensuring robust blocking behavior when the buffer is full or empty.

## 2. Data Integrity Verification
**Challenge:** Ensuring zero data loss or duplication in a concurrent environment with multiple active threads.
**Decision:** Implementation of a **Global Sequence Tracking** mechanism.
**Reasoning:** Tagging every generated item with a unique, incrementing sequence ID allows for a complete audit of the data flow. The final validation step compares the set of produced IDs against consumed IDs. If `Items Produced != Items Consumed` or if there are gaps in the sequence, the system explicitly flags a failure. This transforms verification from a visual check into a mathematical certainty.

## 3. Observability & Debugging
**Challenge:** Standard `print()` statements are not thread-safe and result in garbled, interleaved output that makes debugging concurrency issues difficult.
**Decision:** Adoption of a **Thread-Aware Structured Logging** system.
**Reasoning:** Configuring the `logging` module to include `%(threadName)s` in every log entry allows for tracing the exact execution path of specific threads (e.g., `Producer-1` vs `Consumer-2`) even during simultaneous execution. This visibility is critical for diagnosing edge cases like thread starvation or resource contention.

## 4. Deterministic Output (Out-of-Order Logs)
**Challenge:** In a multi-threaded environment, threads racing to write to `stdout` can result in log lines appearing out of chronological order, even if the events happened sequentially.
**Decision:** Implementation of an **In-Memory Log Buffering & Sorting** strategy.
**Reasoning:** Instead of streaming logs directly to the console, a custom `LogCaptureHandler` captures all log records in memory as they occur. After the simulation completes, these records are sorted by their precise creation timestamp and displayed atomically. This guarantees that the final report represents the true chronological order of events, eliminating the confusion caused by console I/O contention.

## 5. Environment Consistency (Docker)
**Challenge:** Python threading behavior and scheduling can vary significantly between operating systems (Windows vs Linux).
**Decision:** Full application containerization using Docker.
**Reasoning:** Defining the runtime environment in code (`Dockerfile`) ensures that the concurrency logic executes consistently across all platforms, eliminating environment-specific bugs and drift.

## 6. CI/CD Optimization
**Challenge:** Inefficient resource usage when running the full test suite for unrelated changes.
**Decision:** Implementation of **Path Filtering** in the GitHub Actions workflow.
**Reasoning:** The CI pipeline is configured to trigger the Python test suite only when files in the `assignment1/` directory are modified. This targeted testing strategy reduces feedback loops and optimizes compute resource usage.
