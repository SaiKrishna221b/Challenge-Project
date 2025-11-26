"""Performance and scalability tests."""

import pytest
import time
from ProducerConsumer.core import SimulationManager

@pytest.mark.timeout(5)
def test_performance_large_dataset():
    """Verify processing 1000 items completes within 5 seconds (deadlock detection)."""
    start_time = time.perf_counter()
    
    n_items = 1000
    capacity = 50
    manager = SimulationManager(n_items, capacity)
    results = manager.run()
    
    end_time = time.perf_counter()
    duration = end_time - start_time
    
    assert len(results) == n_items
    print(f"\nProcessed {n_items} items in {duration:.4f} seconds")

