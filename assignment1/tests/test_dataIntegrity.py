"""Tests for data integrity and correctness."""

import pytest
from ProducerConsumer.core import SimulationManager

def test_item_ordering_fifo():
    """Verify items are consumed in FIFO order."""
    n_items = 20
    capacity = 5
    manager = SimulationManager(n_items, capacity)
    results = manager.run()
    
    # Items should be in order 1, 2, 3, ..., n_items
    for i, item in enumerate(results):
        assert item.item_id == i + 1, f"Expected item_id={i+1}, got {item.item_id}"

def test_no_duplicate_items():
    """Verify no duplicate items in results."""
    n_items = 50
    capacity = 10
    manager = SimulationManager(n_items, capacity)
    results = manager.run()
    
    item_ids = [item.item_id for item in results]
    assert len(item_ids) == len(set(item_ids)), "Duplicate items found in results"

def test_all_items_present():
    """Verify all expected items are present in results."""
    n_items = 30
    capacity = 8
    manager = SimulationManager(n_items, capacity)
    results = manager.run()
    
    assert len(results) == n_items
    item_ids = {item.item_id for item in results}
    expected_ids = set(range(1, n_items + 1))
    assert item_ids == expected_ids, "Not all expected items are present"

def test_item_ids_match_sequence():
    """Verify item IDs match expected sequence."""
    n_items = 15
    capacity = 3
    manager = SimulationManager(n_items, capacity)
    results = manager.run()
    
    expected_sequence = list(range(1, n_items + 1))
    actual_sequence = [item.item_id for item in results]
    assert actual_sequence == expected_sequence, "Item IDs don't match expected sequence"

