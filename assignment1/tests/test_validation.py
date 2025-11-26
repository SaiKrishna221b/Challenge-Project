"""Tests for input validation and CLI functionality."""

import pytest
import sys
from unittest.mock import patch
from ProducerConsumer.core import SimulationManager
from ProducerConsumer.cli import parse_args, get_valid_input

def test_zero_capacity_error():
    """Queue capacity must be > 0."""
    with pytest.raises(ValueError, match="queue_capacity must be greater than 0"):
        SimulationManager(number_of_items=10, queue_capacity=0)

def test_negative_capacity_error():
    """Queue capacity cannot be negative."""
    with pytest.raises(ValueError, match="queue_capacity must be greater than 0"):
        SimulationManager(number_of_items=10, queue_capacity=-1)

def test_zero_items_error():
    """Number of items must be > 0."""
    with pytest.raises(ValueError, match="number_of_items must be greater than 0"):
        SimulationManager(number_of_items=0, queue_capacity=10)

def test_negative_items_error():
    """Number of items cannot be negative."""
    with pytest.raises(ValueError, match="number_of_items must be greater than 0"):
        SimulationManager(number_of_items=-5, queue_capacity=10)

def test_invalid_type_error():
    """Inputs must be integers."""
    with pytest.raises(TypeError):
        SimulationManager(number_of_items="10", queue_capacity=10)
    with pytest.raises(TypeError):
        SimulationManager(number_of_items=10, queue_capacity="10")

def test_max_boundary_values():
    """Test maximum boundary values (100000 items, 10000 capacity)."""
    manager = SimulationManager(number_of_items=100000, queue_capacity=10000)
    results = manager.run()
    assert len(results) == 100000

# CLI functionality tests
def test_argument_parsing_items():
    """Test --items argument parsing."""
    with patch('sys.argv', ['main.py', '--items', '50', '--capacity', '10']):
        args = parse_args()
        assert args.items == 50
        assert args.capacity == 10

def test_argument_parsing_capacity():
    """Test --capacity argument parsing."""
    with patch('sys.argv', ['main.py', '--items', '100', '--capacity', '20']):
        args = parse_args()
        assert args.items == 100
        assert args.capacity == 20

def test_get_valid_input_within_range():
    """Test get_valid_input with valid input within range."""
    with patch('builtins.input', return_value='50'):
        with patch('sys.stdin.isatty', return_value=True):
            result = get_valid_input("Enter items", min_value=1, max_value=100)
            assert result == 50

def test_get_valid_input_below_min():
    """Test get_valid_input with input below minimum."""
    with patch('builtins.input', side_effect=['0', '10']):
        with patch('sys.stdin.isatty', return_value=True):
            with patch('builtins.print'):  # Suppress error messages
                result = get_valid_input("Enter items", min_value=1, max_value=100)
                assert result == 10

def test_get_valid_input_above_max():
    """Test get_valid_input with input above maximum."""
    with patch('builtins.input', side_effect=['200', '50']):
        with patch('sys.stdin.isatty', return_value=True):
            with patch('builtins.print'):  # Suppress error messages
                result = get_valid_input("Enter items", min_value=1, max_value=100)
                assert result == 50

def test_get_valid_input_invalid_type():
    """Test get_valid_input with invalid input type."""
    with patch('builtins.input', side_effect=['abc', '25']):
        with patch('sys.stdin.isatty', return_value=True):
            with patch('builtins.print'):  # Suppress error messages
                result = get_valid_input("Enter items", min_value=1, max_value=100)
                assert result == 25

def test_range_validation_items():
    """Test range validation for items (1-100000)."""
    with patch('sys.argv', ['main.py', '--items', '0', '--capacity', '10']):
        with patch('builtins.print'):  # Suppress output
            args = parse_args()
            assert args.items == 0  # Parser accepts it, validation happens later

def test_range_validation_capacity():
    """Test range validation for capacity (1-10000)."""
    with patch('sys.argv', ['main.py', '--items', '10', '--capacity', '0']):
        with patch('builtins.print'):  # Suppress output
            args = parse_args()
            assert args.capacity == 0  # Parser accepts it, validation happens later

def test_non_interactive_mode():
    """Test non-interactive mode behavior."""
    with patch('sys.stdin.isatty', return_value=False):
        with pytest.raises(EOFError, match="Interactive input not available"):
            get_valid_input("Enter items", min_value=1, max_value=100)
