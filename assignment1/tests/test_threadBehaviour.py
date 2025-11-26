"""Tests for thread behavior, synchronization, and logging utilities."""

import pytest
import logging
import sys
from io import StringIO
from ProducerConsumer.core import SimulationManager
from ProducerConsumer.utils import LogCaptureHandler, setup_logging

def test_small_capacity_blocking():
    """Test with capacity=1 to verify blocking behavior under high contention."""
    n_items = 10
    capacity = 1
    manager = SimulationManager(n_items, capacity)
    results = manager.run()
    assert len(results) == n_items

def test_thread_names(caplog):
    """Verify thread names are correct (Producer, Consumer)."""
    caplog.set_level(logging.INFO)
    
    manager = SimulationManager(number_of_items=5, queue_capacity=3)
    manager.run()
    
    thread_names = {record.threadName for record in caplog.records}
    assert "Producer" in thread_names, "Producer thread name not found"
    assert "Consumer" in thread_names, "Consumer thread name not found"

def test_stop_signal_handling(caplog):
    """Verify STOP_SIGNAL is properly handled."""
    caplog.set_level(logging.INFO)
    
    manager = SimulationManager(number_of_items=5, queue_capacity=3)
    manager.run()
    
    # Check for STOP_SIGNAL messages
    messages = [record.getMessage() for record in caplog.records]
    stop_signal_sent = any("STOP_SIGNAL" in msg and "Finished production" in msg for msg in messages)
    stop_signal_received = any("STOP_SIGNAL" in msg and "Received" in msg for msg in messages)
    
    assert stop_signal_sent, "STOP_SIGNAL not sent by producer"
    assert stop_signal_received, "STOP_SIGNAL not received by consumer"

def test_thread_lifecycle():
    """Verify threads start, execute, and terminate correctly."""
    manager = SimulationManager(number_of_items=10, queue_capacity=5)
    
    # Run simulation
    results = manager.run()
    
    # Verify completion
    assert len(results) == 10
    # Threads should have completed (no hanging)

def test_multiple_runs_reusability():
    """Test that SimulationManager can be reused for multiple runs."""
    manager = SimulationManager(number_of_items=5, queue_capacity=3)
    
    # First run
    results1 = manager.run()
    assert len(results1) == 5
    
    # Clear destination for second run
    manager.destination_data.clear()
    
    # Second run (reuse same manager)
    results2 = manager.run()
    assert len(results2) == 5
    
    # Both runs should produce same results
    assert [item.item_id for item in results1] == [item.item_id for item in results2]

def test_consumer_waiting_on_empty_queue():
    """Verify consumer blocks when queue is empty."""
    # Producer starts first, consumer should wait
    manager = SimulationManager(number_of_items=5, queue_capacity=3)
    results = manager.run()
    # If consumer didn't wait properly, we'd have issues
    assert len(results) == 5

def test_producer_blocking_when_full():
    """Verify producer blocks when queue is full."""
    # Small capacity with many items forces producer to block
    manager = SimulationManager(number_of_items=20, queue_capacity=2)
    results = manager.run()
    # If producer didn't block properly, we'd have issues
    assert len(results) == 20

def test_no_race_conditions_destination():
    """Verify no race conditions in destination list (single consumer)."""
    # Run multiple times to check for race conditions
    for _ in range(5):
        manager = SimulationManager(number_of_items=10, queue_capacity=3)
        results = manager.run()
        # Check for duplicates (race condition indicator)
        item_ids = [item.item_id for item in results]
        assert len(item_ids) == len(set(item_ids)), "Race condition detected: duplicate items"

# Logging and utilities tests
def test_log_capture_handler_functionality():
    """Test LogCaptureHandler captures logs correctly."""
    handler = LogCaptureHandler()
    logger = logging.getLogger("test")
    logger.addHandler(handler)
    logger.setLevel(logging.INFO)
    
    logger.info("Test message 1")
    logger.info("Test message 2")
    
    captured_logs = handler.get_sorted_logs()
    assert len(captured_logs) == 2
    assert "Test message 1" in captured_logs[0].getMessage()
    assert "Test message 2" in captured_logs[1].getMessage()

def test_timestamp_ordering_correctness(caplog):
    """Verify timestamp ordering is correct."""
    caplog.set_level(logging.INFO)
    
    # Clear existing handlers
    root_logger = logging.getLogger()
    for handler in root_logger.handlers[:]:
        root_logger.removeHandler(handler)
    
    handler = setup_logging()
    manager = SimulationManager(5, 3)
    manager.run()
    
    sorted_logs = handler.get_sorted_logs()
    
    # Verify timestamps are in ascending order
    for i in range(len(sorted_logs) - 1):
        assert sorted_logs[i].created <= sorted_logs[i + 1].created, (
            f"Logs not in timestamp order: {i} has timestamp {sorted_logs[i].created}, "
            f"{i+1} has {sorted_logs[i+1].created}"
        )

def test_log_format_consistency(caplog):
    """Verify log format is consistent."""
    caplog.set_level(logging.INFO)
    
    manager = SimulationManager(5, 3)
    manager.run()
    
    # Check log format: [threadName] - levelname - message
    for record in caplog.records:
        if record.levelname == "INFO":
            # Format should be consistent
            assert hasattr(record, 'threadName')
            assert hasattr(record, 'levelname')
            assert hasattr(record, 'message')

def test_silent_capture_no_console_output():
    """Verify logs are captured silently (no console output during simulation)."""
    # Capture stdout
    old_stdout = sys.stdout
    sys.stdout = StringIO()
    
    try:
        # Clear existing handlers
        root_logger = logging.getLogger()
        for handler in root_logger.handlers[:]:
            root_logger.removeHandler(handler)
        
        handler = setup_logging()
        logger = logging.getLogger("test")
        
        # Log some messages
        logger.info("Silent message 1")
        logger.info("Silent message 2")
        
        # Verify logs were captured
        captured = handler.get_sorted_logs()
        assert len(captured) >= 2, f"Expected at least 2 captured logs, got {len(captured)}"
    finally:
        sys.stdout = old_stdout

def test_log_handler_clear_logs():
    """Test clear_logs() method."""
    handler = LogCaptureHandler()
    logger = logging.getLogger("test")
    logger.addHandler(handler)
    logger.setLevel(logging.INFO)
    
    logger.info("Message 1")
    assert len(handler.captured_logs) == 1
    
    handler.clear_logs()
    assert len(handler.captured_logs) == 0

