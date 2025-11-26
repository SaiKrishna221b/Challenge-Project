"""
Producer-Consumer Pattern Implementation with Thread Synchronization

Implements the classic producer-consumer pattern using queue.Queue, which internally
uses threading.Condition for wait/notify synchronization.

Wait/Notify Mechanism:
queue.Queue uses threading.Lock and threading.Condition to coordinate threads:
- put(): Blocks on 'not_full' condition when queue is full
- get(): Blocks on 'not_empty' condition when queue is empty
- Automatic notification between producer and consumer threads
"""

import threading
import queue
import logging
from typing import List
from .models import WorkItem

STOP_SIGNAL = None

class Producer(threading.Thread):
    """Producer thread that creates WorkItems and places them into a shared queue."""
    
    def __init__(self, source_ids: List[int], shared_queue: queue.Queue, name: str = "Producer"):
        super().__init__(name=name)
        self.source_ids = source_ids
        self.shared_queue = shared_queue
        self.logger = logging.getLogger(__name__)

    def run(self) -> None:
        """Main execution loop: creates WorkItems and adds them to the queue."""
        self.logger.info("Starting")
        for item_id in self.source_ids:
            item = WorkItem(item_id)
            self.shared_queue.put(item)  # Blocks if queue full
            buffer_after = self.shared_queue.qsize()
            buffer_before = buffer_after - 1  # Calculate before state to avoid race condition
            # Add extra space for single-digit IDs for alignment
            spacing = "   " if item_id < 10 else "  "
            self.logger.info(f"Produced {item}{spacing}|  Buffer: {buffer_before} -> {buffer_after}")
        
        self.shared_queue.put(STOP_SIGNAL)
        buffer_after = self.shared_queue.qsize()
        buffer_before = buffer_after - 1  # Calculate before state to avoid race condition
        self.logger.info(f"Finished production. Sent STOP_SIGNAL.  |  Buffer: {buffer_before} -> {buffer_after}")

class Consumer(threading.Thread):
    """Consumer thread that retrieves WorkItems from the shared queue."""
    
    def __init__(self, shared_queue: queue.Queue, destination: List[WorkItem], name: str = "Consumer"):
        super().__init__(name=name)
        self.shared_queue = shared_queue
        self.destination = destination
        self.logger = logging.getLogger(__name__)

    def run(self) -> None:
        """Main execution loop: retrieves items from queue until STOP_SIGNAL received."""
        self.logger.info("Starting")
        while True:
            item = self.shared_queue.get()  # Blocks if queue empty
            buffer_after = self.shared_queue.qsize()
            buffer_before = buffer_after + 1  # Calculate before state to avoid race condition
            
            if item is STOP_SIGNAL:
                self.logger.info(f"Received STOP_SIGNAL. Quitting.  |  Buffer: {buffer_before} -> {buffer_after}")
                self.shared_queue.task_done()
                break
            
            self.destination.append(item)
            # Add extra space for single-digit IDs for alignment
            spacing = "   " if item.item_id < 10 else "  "
            self.logger.info(f"Consumed {item}{spacing}|  Buffer: {buffer_before} -> {buffer_after}")
            self.shared_queue.task_done()

class SimulationManager:
    """Orchestrates producer and consumer threads with a shared queue."""
    
    def __init__(self, number_of_items: int, queue_capacity: int):
        if not isinstance(number_of_items, int):
            raise TypeError(f"number_of_items must be an integer, got {type(number_of_items).__name__}")
        if not isinstance(queue_capacity, int):
            raise TypeError(f"queue_capacity must be an integer, got {type(queue_capacity).__name__}")
        
        if number_of_items <= 0:
            raise ValueError(f"number_of_items must be greater than 0, got {number_of_items}")
        if queue_capacity <= 0:
            raise ValueError(f"queue_capacity must be greater than 0, got {queue_capacity}")
        
        self.number_of_items = number_of_items
        self.queue_capacity = queue_capacity
        self.source_data = list(range(1, number_of_items + 1))
        self.destination_data: List[WorkItem] = []
        self.queue: queue.Queue = queue.Queue(maxsize=queue_capacity)

    def run(self) -> List[WorkItem]:
        """Executes the simulation and returns processed items."""
        producer = Producer(self.source_data, self.queue)
        consumer = Consumer(self.queue, self.destination_data)

        producer.start()
        consumer.start()

        producer.join()
        consumer.join()

        return self.destination_data

