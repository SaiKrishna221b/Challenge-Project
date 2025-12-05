"""Tests for buffer state transitions and logical consistency.

This is a "White Box" test that inspects internal state changes.
It verifies that the shared queue behaves correctly as a Bounded Buffer.
"""

import pytest
import re
import logging
from ProducerConsumer.core import SimulationManager

def test_buffer_state_continuity(caplog):
    """
    Verify that buffer state transitions are logically consistent.
    
    The Challenge:
        In a multi-threaded system, logs capture a snapshot of the state.
        However, between the time an item is added and the log is written,
        another thread might have changed the state.
        
    The Strategy:
        We analyze the log stream to ensure:
        1. **Boundaries**: State never exceeds capacity or drops below 0.
        2. **Direction**: 'Produce' operations should generally increase count,
           and 'Consume' operations should decrease it.
        3. **Finality**: The system must end with an empty buffer (State 0).
    """
    # Capture INFO level logs to analyze output
    caplog.set_level(logging.INFO)
    
    n_items = 20
    capacity = 5
    manager = SimulationManager(n_items, capacity)
    manager.run()
    
    # Regex to extract state from logs: "Buffer: 3 -> 4"
    buffer_pattern = re.compile(r'Buffer:\s*(\d+)\s*->\s*(\d+)')
    buffer_transitions = []
    operation_types = []
    
    # Parse the logs
    for record in caplog.records:
        message = record.getMessage()
        match = buffer_pattern.search(message)
        if match:
            before_state = int(match.group(1))
            after_state = int(match.group(2))
            buffer_transitions.append((before_state, after_state))
            
            # Categorize operation
            if "Produced" in message or "Finished production" in message:
                operation_types.append("produce")
            elif "Consumed" in message or "Received STOP_SIGNAL" in message:
                operation_types.append("consume")
            else:
                operation_types.append("unknown")
    
    # --- VERIFICATION PHASE ---
    
    # 1. Verify we actually got data
    assert len(buffer_transitions) > 0, "No buffer state transitions found in logs"
    
    # 2. Boundary Checks (Safety Property)
    # The buffer should NEVER report a size < 0 or > capacity.
    for before_state, after_state in buffer_transitions:
        assert 0 <= before_state <= capacity, (
            f"Invalid before state: {before_state} (must be 0-{capacity})"
        )
        assert 0 <= after_state <= capacity, (
            f"Invalid after state: {after_state} (must be 0-{capacity})"
        )
    
    # 3. Transition Logic Checks (Liveness Property)
    for i, ((before_state, after_state), op_type) in enumerate(zip(buffer_transitions, operation_types)):
        delta = after_state - before_state
        
        if op_type == "produce":
            # Producer adds an item -> Delta should be +1 (or 0 if highly contended/approximate)
            # It should NEVER be negative (Producer removing an item? Impossible!)
            assert delta >= 0, f"Producer operation {i} showed impossible buffer decrease"
            
        elif op_type == "consume":
            # Consumer removes an item -> Delta should be -1
            # It should NEVER be positive (Consumer adding an item? Impossible!)
            assert delta <= 0, f"Consumer operation {i} showed impossible buffer increase"
    
    # 4. Final State Check
    # After everything is done, the buffer must be empty.
    # If it's not 0, we have a "Memory Leak" (stranded items).
    final_transition = buffer_transitions[-1]
    assert final_transition[1] == 0, (
        f"Final buffer state should be 0, but got {final_transition[1]}"
    )
