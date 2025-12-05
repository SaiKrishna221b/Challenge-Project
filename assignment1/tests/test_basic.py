"""Basic production and consumption functional tests.

This module validates the fundamental contract of the Producer-Consumer system:
"What goes in must come out."
"""

import pytest
from ProducerConsumer.core import SimulationManager

def test_basic_production_consumption():
    """
    Verify input count matches output count and data integrity.
    
    Scenario:
        - Produce 50 items.
        - Use a buffer capacity of 10 (forcing multiple blocking/waking cycles).
    
    Assertions:
        1. Total processed items equals 50.
        2. First item ID is 1.
        3. Last item ID is 50.
    """
    n_items = 50
    capacity = 10
    
    # Initialize the manager
    manager = SimulationManager(n_items, capacity)
    
    # Execute the simulation (blocks until completion)
    results = manager.run()
    
    # Verify the results
    assert len(results) == n_items, f"Expected {n_items} results, got {len(results)}"
    
    # Verify data integrity (items weren't scrambled beyond recognition)
    # Note: The Manager sorts results by sequence_number before returning,
    # so we expect a perfect 1..50 sequence here.
    assert results[0].item_id == 1
    assert results[-1].item_id == 50
