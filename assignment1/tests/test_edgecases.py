"""Tests for edge cases and boundary conditions.

These tests push the system to limits to ensure robustness.
"What happens if we have only 1 item?"
"What happens if the buffer is tiny?"
"""

import pytest
from ProducerConsumer.core import SimulationManager

def test_single_item():
    """
    Test with the absolute minimum workload (n_items=1).
    
    Why: Ensures no off-by-one errors in loops or partitioning logic.
    """
    manager = SimulationManager(number_of_items=1, queue_capacity=5)
    results = manager.run()
    assert len(results) == 1
    assert results[0].item_id == 1

def test_capacity_equals_items():
    """
    Test when capacity equals number of items.
    
    Why: In this case, blocking *should not* occur (theoretically).
    The Producer should be able to dump everything without waiting for a Consumer.
    """
    n_items = 10
    capacity = 10
    manager = SimulationManager(number_of_items=n_items, queue_capacity=capacity)
    results = manager.run()
    assert len(results) == n_items

def test_very_large_capacity():
    """
    Test with huge capacity relative to items (Capacity >> Items).
    
    Why: Ensures the system doesn't waste resources or crash if allocated memory
    is much larger than needed.
    """
    manager = SimulationManager(number_of_items=10, queue_capacity=1000)
    results = manager.run()
    assert len(results) == 10

def test_extreme_contention():
    """
    Test capacity=1 with many items (Extreme Contention).
    
    Why: This forces maximum synchronization overhead.
    Every single produce operation blocks until a consume happens, and vice versa.
    It's a stress test for the Wait/Notify mechanism.
    """
    manager = SimulationManager(number_of_items=100, queue_capacity=1)
    results = manager.run()
    assert len(results) == 100
