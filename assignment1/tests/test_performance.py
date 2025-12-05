"""Performance and scalability tests.

These tests act as "canaries in the coal mine" for deadlocks.
If the code hangs, the timeout decorator kills the test and fails it.
"""

import pytest
import time
from ProducerConsumer.core import SimulationManager

# Enforce a strict 5-second timeout. 
# If the simulation takes longer, it's likely deadlocked or horribly inefficient.
@pytest.mark.timeout(5)
def test_performance_large_dataset():
    """
    Verify processing 1000 items completes within 5 seconds.
    
    This implies:
    1. Threads are actually running in parallel (or effectively context switching).
    2. No thread is getting stuck waiting for a signal that never comes (Deadlock).
    """
    start_time = time.perf_counter()
    
    n_items = 1000
    capacity = 50
    manager = SimulationManager(n_items, capacity)
    results = manager.run()
    
    end_time = time.perf_counter()
    duration = end_time - start_time
    
    assert len(results) == n_items
    print(f"\nProcessed {n_items} items in {duration:.4f} seconds")
