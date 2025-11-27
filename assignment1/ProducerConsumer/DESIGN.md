# ProducerConsumer Design Notes

This document captures the main design decisions behind the Assignment 1 implementation. The goal is to build a thread-safe producer–consumer simulation that scales beyond a single pair of threads while remaining easy to reason about.

---

## Architecture Overview

- **Producer threads** own disjoint slices of the item range and feed a shared `queue.Queue`.  
- **Consumer threads** drain that queue into thread-local buffers so they never fight over a shared list.  
- **SimulationManager** orchestrates creation, sequencing, STOP-signal injection, and result collation.  
- **CLI + logging utilities** provide an easy way to run scenarios interactively or from the command line.


## Key Design Choices

### 1. Built on `queue.Queue`
Using the standard library queue means we inherit correct blocking and wait/notify behavior implemented with `threading.Lock` + `threading.Condition`. That avoids reinventing synchronization primitives and lets us focus on higher-level concerns (logging, ordering, validation).

### 2. Sequence Numbers for Global Ordering
Multiple producers can interleave puts in any order, so we assign every `WorkItem` a monotonically increasing `sequence_number`. After consumption, the manager sorts by this number to restore a FIFO view of the work, regardless of how producers or consumers were scheduled.

### 3. Thread-Local Consumer Buffers
Each `Consumer` stores items in a simple list that only that thread touches. This eliminates locking around result aggregation and keeps contention limited to the queue. The manager merges these buffers once all consumers exit.

### 4. Sentinel-Based Shutdown
Producers notify the manager when they finish their chunk. Once *all* producers are done, the manager enqueues one `STOP_SIGNAL` per consumer. This ensures every consumer wakes up, sees a sentinel, and exits gracefully without needing extra coordination state.

### 5. Reusable SimulationManager
`SimulationManager.run()` resets its internal queue, destination buffer, and sequence counter on every invocation. That makes it safe to instantiate once and reuse in tests or repeated CLI runs without accumulating stale state.

### 6. Logging Strategy
Because locking is handled entirely inside `queue.Queue`, producer and consumer log messages can interleave in any order. We capture every record in a custom handler and sort by timestamp before displaying them so the CLI and tests read like a coherent narrative. This also lets us assert on buffer state transitions, STOP-signal delivery, and lifecycle events without relying on thread scheduling.

### 7. Destination Ordering Guarantees
Consumers append to thread-local buffers, but the manager merges and sorts the combined list by `sequence_number` before returning it. The destination container therefore matches the logical FIFO order of the source data even though the work happened in parallel.

---

## Future Considerations

1. **Metrics hooks**: Expose counters/timers for throughput analysis.  
2. **Pluggable backpressure**: allow swapping `queue.Queue` for custom bounded buffers (e.g., asyncio queue).  
3. **Fault injection**: optionally simulate producer or consumer failures to demonstrate recovery strategies.  
4. **Work item payloads**: `WorkItem` currently holds `item_id` + `sequence_number`, but it can easily grow additional fields (payloads, metadata, timestamps) without touching the threading logic thanks to its immutability.

---

## Testing Strategy Summary

- **Data integrity**: ensures the output set matches the expected ID range with no duplicates.  
- **Concurrency behavior**: verifies STOP-signal handling, thread naming, buffer transitions, and blocking semantics even under capacity contention.  
- **Performance**: sanity-checks that 1,000-item runs complete in a reasonable time to catch deadlocks.  
- **CLI validation**: covers argument parsing, interactive prompts, and error surfaces for invalid ranges.

---

Questions or suggestions? Update this document or raise an issue so future contributors understand the rationale behind the current design.

