"""Lightweight data structures shared between producers and consumers."""

from dataclasses import dataclass

@dataclass(frozen=True)
class WorkItem:
    """Immutable payload tagged with a global sequence number."""
    item_id: int
    sequence_number: int = 0

    def __repr__(self) -> str:
        """String representation showing the item ID and sequence number."""
        return f"WorkItem(id={self.item_id}, seq={self.sequence_number})"

