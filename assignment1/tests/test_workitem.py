"""Tests for WorkItem model."""

import pytest
from ProducerConsumer.models import WorkItem

def test_workitem_creation():
    """Test WorkItem creation with valid ID."""
    item = WorkItem(item_id=1)
    assert item.item_id == 1

def test_workitem_immutability():
    """Test that WorkItem is immutable (frozen dataclass)."""
    item = WorkItem(item_id=5)
    with pytest.raises(Exception):  # dataclasses.FrozenInstanceError
        item.item_id = 10

def test_workitem_repr():
    """Test WorkItem string representation."""
    item = WorkItem(item_id=42)
    assert repr(item) == "WorkItem(id=42, seq=0)"

def test_workitem_equality():
    """Test WorkItem equality comparison."""
    item1 = WorkItem(item_id=1)
    item2 = WorkItem(item_id=1)
    item3 = WorkItem(item_id=2)
    
    assert item1 == item2
    assert item1 != item3

