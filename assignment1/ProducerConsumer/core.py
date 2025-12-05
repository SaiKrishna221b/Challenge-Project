"""Concurrent producer-consumer simulation built on top of queue.Queue.

queue.Queue already wraps a lock/condition pair, so we get blocking put/get
behavior. This module layers logging, sequencing, and thread
coordination so multiple producers and consumers can share the buffer safely.
"""

import threading
import queue
import logging
from typing import List, Optional
from .models import WorkItem

STOP_SIGNAL = None

class Producer(threading.Thread):
    """Feeds the shared queue with WorkItems while preserving global order."""
    
    def __init__(self, source_ids: List[int], shared_queue: queue.Queue, 
                 sequence_counter: Optional[threading.Lock] = None,
                 sequence_value: Optional[List[int]] = None,
                 name: str = "Producer"):
        super().__init__(name=name)
        self.source_ids = source_ids
        self.shared_queue = shared_queue
        self.sequence_counter = sequence_counter  # Guards the shared sequence counter
        self.sequence_value = sequence_value  # Mutable single-value store for the counter
        self.logger = logging.getLogger(__name__)

    def run(self) -> None:
        """Emit items from the assigned source chunk and hand them to the queue."""
        self.logger.info("Starting")
        for item_id in self.source_ids:
            # Stamp each item with a globally increasing sequence number
            if self.sequence_counter and self.sequence_value:
                with self.sequence_counter:
                    seq_num = self.sequence_value[0]
                    self.sequence_value[0] += 1
            else:
                seq_num = 0  # Fallback path when sequence bookkeeping is disabled
            
            item = WorkItem(item_id, sequence_number=seq_num)
            # BLOCKS if full (Internally calls not_full.wait() to release lock)
            self.shared_queue.put(item)
            buffer_after = self.shared_queue.qsize()
            buffer_before = buffer_after - 1  # Derive the before-state while we hold the slot
            # Pad single-digit IDs for nicer log alignment
            spacing = "   " if item_id < 10 else "  "
            self.logger.info(
                f"Produced WorkItem(id={item.item_id}){spacing}|  Buffer: {buffer_before} -> {buffer_after}"
            )
        
        self.logger.info("Finished production")
        # STOP signals are injected by the manager once every producer is done

class Consumer(threading.Thread):
    """Drains WorkItems from the shared queue into a private buffer."""
    
    def __init__(self, shared_queue: queue.Queue, name: str = "Consumer"):
        super().__init__(name=name)
        self.shared_queue = shared_queue
        self.local_destination: List[WorkItem] = []  # Local buffer keeps locking simple
        self.logger = logging.getLogger(__name__)
    
    def get_destination(self) -> List[WorkItem]:
        """Expose the items consumed by this thread."""
        return self.local_destination

    def run(self) -> None:
        """Drain queue items until the manager posts our STOP signal."""
        self.logger.info("Starting")
        while True:
            # BLOCKS if empty (Internally calls not_empty.wait() to release lock)
            item = self.shared_queue.get()
            buffer_after = self.shared_queue.qsize()
            buffer_before = buffer_after + 1  # Capture the before-state while we own the slot
            
            if item is STOP_SIGNAL:
                self.logger.info(f"Received STOP_SIGNAL. Quitting.  |  Buffer: {buffer_before} -> {buffer_after}")
                self.shared_queue.task_done()
                break
            
            self.local_destination.append(item)
            # Pad single-digit IDs for nicer log alignment
            spacing = "   " if item.item_id < 10 else "  "
            self.logger.info(
                f"Consumed WorkItem(id={item.item_id}){spacing}|  Buffer: {buffer_before} -> {buffer_after}"
            )
            self.shared_queue.task_done()

class SimulationManager:
    """Coordinates thread creation, sequencing, and orderly shutdown."""
    
    def __init__(self, number_of_items: int, queue_capacity: int, 
                 num_producers: int = 1, num_consumers: int = 1):
        if not isinstance(number_of_items, int):
            raise TypeError(f"number_of_items must be an integer, got {type(number_of_items).__name__}")
        if not isinstance(queue_capacity, int):
            raise TypeError(f"queue_capacity must be an integer, got {type(queue_capacity).__name__}")
        if not isinstance(num_producers, int):
            raise TypeError(f"num_producers must be an integer, got {type(num_producers).__name__}")
        if not isinstance(num_consumers, int):
            raise TypeError(f"num_consumers must be an integer, got {type(num_consumers).__name__}")
        
        if number_of_items <= 0:
            raise ValueError(f"number_of_items must be greater than 0, got {number_of_items}")
        if queue_capacity <= 0:
            raise ValueError(f"queue_capacity must be greater than 0, got {queue_capacity}")
        if num_producers <= 0:
            raise ValueError(f"num_producers must be greater than 0, got {num_producers}")
        if num_consumers <= 0:
            raise ValueError(f"num_consumers must be greater than 0, got {num_consumers}")
        
        self.number_of_items = number_of_items
        self.queue_capacity = queue_capacity
        self.num_producers = num_producers
        self.num_consumers = num_consumers
        
        # Pre-slice the item range so each producer owns a deterministic chunk
        self.source_data_chunks = self._distribute_items()
        
        # Shared counter for globally ordered results
        self.sequence_counter = threading.Lock()
        self.sequence_value = [0]  # Single-element list so we can mutate by reference
        
        self.queue_capacity = queue_capacity
        self.queue: queue.Queue = queue.Queue(maxsize=queue_capacity)
        self.destination_data: List[WorkItem] = []
    
    def _distribute_items(self) -> List[List[int]]:
        """Return balanced slices so producers split the work as evenly as possible."""
        source_data = list(range(1, self.number_of_items + 1))
        chunk_size = self.number_of_items // self.num_producers
        remainder = self.number_of_items % self.num_producers
        
        chunks = []
        start = 0
        for i in range(self.num_producers):
            # Hand remainder items to the earliest producers to keep gaps minimal
            size = chunk_size + (1 if i < remainder else 0)
            chunks.append(source_data[start:start + size])
            start += size
        
        return chunks

    def run(self) -> List[WorkItem]:
        """Spin up the configured threads and return results sorted by sequence number."""
        # Reset mutable state so the manager can be reused safely
        self.sequence_value[0] = 0
        self.destination_data.clear()
        self.queue = queue.Queue(maxsize=self.queue_capacity)

        # Create producers
        producers = []
        for i, source_chunk in enumerate(self.source_data_chunks):
            producer = Producer(
                source_ids=source_chunk,
                shared_queue=self.queue,
                sequence_counter=self.sequence_counter,
                sequence_value=self.sequence_value,
                name=f"Producer-{i+1}"
            )
            producers.append(producer)
        
        # Create consumers
        consumers = []
        for i in range(self.num_consumers):
            consumer = Consumer(
                shared_queue=self.queue,
                name=f"Consumer-{i+1}"
            )
            consumers.append(consumer)
        
        # Launch everyone
        for producer in producers:
            producer.start()
        for consumer in consumers:
            consumer.start()
        
        # Wait for every producer to exhaust its chunk
        for producer in producers:
            producer.join()
        
        # Post one STOP signal per consumer once production stops
        for _ in range(self.num_consumers):
            self.queue.put(STOP_SIGNAL)
        
        # Drain consumers once they acknowledge the sentinel
        for consumer in consumers:
            consumer.join()
        
        # Merge each consumer's private buffer
        all_results = []
        for consumer in consumers:
            all_results.extend(consumer.get_destination())
        
        # Sequence numbers restore the original ordering regardless of interleaving
        all_results.sort(key=lambda item: item.sequence_number)

        self.destination_data.extend(all_results)
        
        return self.destination_data

