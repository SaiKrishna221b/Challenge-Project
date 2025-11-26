"""Basic production and consumption tests."""

import pytest
from ProducerConsumer.core import SimulationManager

def test_basic_production_consumption():
    """Verify input count matches output count and data integrity."""
    n_items = 50
    capacity = 10
    manager = SimulationManager(n_items, capacity)
    results = manager.run()
    
    assert len(results) == n_items
    assert results[0].item_id == 1
    assert results[-1].item_id == 50
