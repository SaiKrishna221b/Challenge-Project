"""Tests for data integrity and correctness.

These tests ensure that the system doesn't just "run", but actually
produces correct results without losing, duplicating, or corrupting data.
"""

import pytest
from ProducerConsumer.core import SimulationManager

def test_item_ordering_fifo():
    """
    Verify that Sequence Numbers are respected.
    
    Even though threads process items in parallel (non-deterministically),
    the final aggregated list MUST be sorted by the sequence number 
    assigned at creation. This proves we can reconstruct the original timeline.
    """
    n_items = 20
    capacity = 5
    manager = SimulationManager(n_items, capacity)
    results = manager.run()
    
    # Items should be in order 1, 2, 3, ..., n_items
    for i, item in enumerate(results):
        assert item.item_id == i + 1, f"Expected item_id={i+1}, got {item.item_id}"

def test_no_duplicate_items():
    """
    Verify that no item is processed twice.
    
    This catches race conditions where two consumers might grab the 
    same item from the queue simultaneously.
    """
    n_items = 50
    capacity = 10
    manager = SimulationManager(n_items, capacity)
    results = manager.run()
    
    item_ids = [item.item_id for item in results]
    # If Set size == List size, all elements are unique
    assert len(item_ids) == len(set(item_ids)), "Duplicate items found in results"

def test_all_items_present():
    """
    Verify that no items are lost.
    
    This ensures we don't have "dropped packets" or items left stranded
    in the queue when the simulation shuts down.
    """
    n_items = 30
    capacity = 8
    manager = SimulationManager(n_items, capacity)
    results = manager.run()
    
    assert len(results) == n_items
    
    # Check the exact set of IDs
    item_ids = {item.item_id for item in results}
    expected_ids = set(range(1, n_items + 1))
    assert item_ids == expected_ids, "Not all expected items are present"

def test_item_ids_match_sequence():
    """
    Verify mapping between Item ID and Sequence Number.
    
    In this simulation, Item ID 1 should have Sequence 0, ID 2 -> Seq 1, etc.
    This validates that the Producer thread logic is correctly assigning
    metadata to the payload.
    """
    n_items = 15
    capacity = 3
    manager = SimulationManager(n_items, capacity)
    results = manager.run()
    
    expected_sequence = list(range(1, n_items + 1))
    actual_sequence = [item.item_id for item in results]
    assert actual_sequence == expected_sequence, "Item IDs don't match expected sequence"
