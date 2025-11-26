"""Tests for edge cases and boundary conditions."""

import pytest
from ProducerConsumer.core import SimulationManager

def test_single_item():
    """Test with single item (n_items=1)."""
    manager = SimulationManager(number_of_items=1, queue_capacity=5)
    results = manager.run()
    assert len(results) == 1
    assert results[0].item_id == 1

def test_capacity_equals_items():
    """Test when capacity equals number of items (no blocking needed)."""
    n_items = 10
    capacity = 10
    manager = SimulationManager(number_of_items=n_items, queue_capacity=capacity)
    results = manager.run()
    assert len(results) == n_items

def test_very_large_capacity():
    """Test with very large capacity relative to items (capacity >> n_items)."""
    manager = SimulationManager(number_of_items=10, queue_capacity=1000)
    results = manager.run()
    assert len(results) == 10

def test_extreme_contention():
    """Test capacity=1 with many items (extreme contention)."""
    manager = SimulationManager(number_of_items=100, queue_capacity=1)
    results = manager.run()
    assert len(results) == 100

