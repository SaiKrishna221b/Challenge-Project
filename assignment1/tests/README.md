# Assignment 1 Test Suite

This document summarizes every test module under `assignment1/tests/`, the scenarios it covers, and ideas for future additions.

## Existing Test Modules

| File | Focus | Highlights |
| --- | --- | --- |
| `test_basic.py` | Happy-path run | Ensures the number of produced items equals the number consumed, and verifies first/last IDs to catch ordering regressions. |
| `test_bufferState.py` | Buffer integrity | Parses log output to prove that buffer transitions stay within `[0, capacity]`, deltas are consistent with produce/consume operations, and STOP signals drain the queue completely. |
| `test_dataIntegrity.py` | FIFO + uniqueness | Validates that results contain exactly the expected IDs, in order, with no duplicates across different capacities. |
| `test_edgecases.py` | Boundary values | Exercises tiny queues, capacity=items, a single item, and high-contention (`capacity=1`) scenarios to ensure no deadlocks or race conditions emerge under extremes. |
| `test_log_saving.py` | Log persistence | Stubs the clock, runs `maybe_save_logs(auto_save=True)`, and asserts that `simulation_logs/YYYY/MM/DD.txt` is created with the captured log contents. |
| `test_performance.py` | Throughput sanity | Runs 1,000 items with a moderate buffer under a pytest timeout to catch deadlocks or pathological performance regressions. |
| `test_threadBehaviour.py` | Concurrency semantics | Covers thread naming, STOP-signal propagation, blocking behavior for full/empty queues, reusability of the manager, logging helper utilities, and race-condition detection in the destination buffer. |
| `test_validation.py` | Input/CLI validation | Exercises constructor type/value checks, CLI argument parsing, and interactive prompts (via mocks) to ensure invalid input surfaces friendly errors. |
| `test_workitem.py` | `WorkItem` contract | Confirms creation, immutability (`frozen=True`), `repr`, and equality semantics for the dataclass. |

