"""Data models for work items transferred between producer and consumer threads."""

from dataclasses import dataclass

@dataclass(frozen=True)
class WorkItem:
    """
    Immutable work item for producer-consumer pattern.
    
    Attributes:
        item_id: Unique identifier for the work item
        
    Thread Safety:
        frozen=True ensures immutability, making the object thread-safe without
        additional synchronization primitives.
    """
    item_id: int

    def __repr__(self) -> str:
        """String representation showing the item ID."""
        return f"WorkItem(id={self.item_id})"

