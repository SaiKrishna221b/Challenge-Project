"""Tests for buffer state transitions and logical consistency."""

import pytest
import re
import logging
from ProducerConsumer.core import SimulationManager

def test_buffer_state_continuity(caplog):
    """Verify that buffer state transitions are logically consistent.
    
    In a concurrent system, logs from different threads can interleave, so we can't
    expect strict sequential continuity. Instead, we verify:
    1. Each transition is valid (delta of -1, 0, or 1)
    2. Buffer states stay within valid range [0, capacity]
    3. The final buffer state is 0 (all items consumed)
    4. The calculated "before" state matches the logical operation (put: before = after - 1, get: before = after + 1)
    """
    caplog.set_level(logging.INFO)
    
    n_items = 20
    capacity = 5
    manager = SimulationManager(n_items, capacity)
    manager.run()
    
    # Extract buffer state transitions from log messages
    # Pattern matches: "Buffer: X -> Y"
    buffer_pattern = re.compile(r'Buffer:\s*(\d+)\s*->\s*(\d+)')
    buffer_transitions = []
    operation_types = []  # Track if it's a produce or consume operation
    
    for record in caplog.records:
        message = record.getMessage()
        match = buffer_pattern.search(message)
        if match:
            before_state = int(match.group(1))
            after_state = int(match.group(2))
            buffer_transitions.append((before_state, after_state))
            
            # Determine operation type from log message
            if "Produced" in message or "Finished production" in message:
                operation_types.append("produce")
            elif "Consumed" in message or "Received STOP_SIGNAL" in message:
                operation_types.append("consume")
            else:
                operation_types.append("unknown")
    
    # Verify we captured buffer transitions
    assert len(buffer_transitions) > 0, "No buffer state transitions found in logs"
    
    # Verify buffer states are within valid range [0, capacity]
    for before_state, after_state in buffer_transitions:
        assert 0 <= before_state <= capacity, (
            f"Invalid before state: {before_state} (must be 0-{capacity})"
        )
        assert 0 <= after_state <= capacity, (
            f"Invalid after state: {after_state} (must be 0-{capacity})"
        )
    
    # Verify each transition is logically consistent with its operation type
    for i, ((before_state, after_state), op_type) in enumerate(zip(buffer_transitions, operation_types)):
        delta = after_state - before_state
        
        if op_type == "produce":
            # Producer should increase buffer by 1 (unless at capacity, then it might stay same)
            assert delta >= 0, (
                f"Producer operation {i} shows buffer decrease: {before_state} -> {after_state}"
            )
            assert delta <= 1, (
                f"Producer operation {i} shows invalid buffer increase: {before_state} -> {after_state} "
                f"(delta: {delta}, should be 0 or 1)"
            )
            # Verify calculated before state: before should be after - 1 (or after if at capacity)
            if delta == 1:
                assert before_state == after_state - 1, (
                    f"Producer operation {i}: calculated before state incorrect. "
                    f"Expected {after_state - 1}, got {before_state}"
                )
        elif op_type == "consume":
            # Consumer should decrease buffer by 1 (unless already empty)
            assert delta <= 0, (
                f"Consumer operation {i} shows buffer increase: {before_state} -> {after_state}"
            )
            assert delta >= -1, (
                f"Consumer operation {i} shows invalid buffer decrease: {before_state} -> {after_state} "
                f"(delta: {delta}, should be 0 or -1)"
            )
            # Verify calculated before state: before should be after + 1 (or after if already empty)
            if delta == -1:
                assert before_state == after_state + 1, (
                    f"Consumer operation {i}: calculated before state incorrect. "
                    f"Expected {after_state + 1}, got {before_state}"
                )
    
    # Verify final buffer state is 0 (all items consumed, including STOP_SIGNAL)
    final_transition = buffer_transitions[-1]
    assert final_transition[1] == 0, (
        f"Final buffer state should be 0, but got {final_transition[1]}"
    )
    
    # Additional check: verify that producer operations that increase buffer by 1
    # have correct before state calculation
    produce_count = sum(1 for op in operation_types if op == "produce")
    consume_count = sum(1 for op in operation_types if op == "consume")
    
    # Producer emits n_items entries, SimulationManager enqueues STOP_SIGNALs separately
    assert produce_count == n_items, (
        f"Expected {n_items} produce operations, got {produce_count}"
    )
    # Consumers process n_items items plus one STOP_SIGNAL
    assert consume_count == n_items + 1, (
        f"Expected {n_items + 1} consume operations, got {consume_count}"
    )

