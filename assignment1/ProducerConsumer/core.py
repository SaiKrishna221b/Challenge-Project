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
        """
        Initialize the Producer thread.
        
        Args:
            source_ids: The list of raw integers this specific producer is responsible for processing.
            shared_queue: The thread-safe buffer where processed WorkItems are placed.
            sequence_counter: A shared Lock to ensure only one thread updates the global sequence at a time.
            sequence_value: A shared mutable list (acting as a pointer) holding the current global sequence number.
            name: A human-readable identifier for this thread (e.g., "Producer-1").
        """
        # CRITICAL: Initialize the parent Thread class first.
        # This sets up the internal thread state so .start() and .join() work correctly.
        # We pass 'name' so logs show "Producer-1" instead of "Thread-5".
        super().__init__(name=name)
        
        self.source_ids = source_ids
        self.shared_queue = shared_queue
        self.sequence_counter = sequence_counter  # Guards the shared sequence counter
        self.sequence_value = sequence_value  # Mutable single-value store for the counter
        self.logger = logging.getLogger(__name__)

    def run(self) -> None:
        """
        The main execution loop for the Producer thread.
        
        1. Iterates through its assigned 'source_ids'.
        2. Acquires a global lock to generate a unique sequence number.
        3. Wraps the data into a 'WorkItem'.
        4. Places the item into the 'shared_queue'.
           - IMPORTANT: This is a BLOCKING operation.
           - If the queue is full, this thread sleeps until space is available.
        """
        self.logger.info("Starting")
        for item_id in self.source_ids:
            # STEP 1: Generate Global Sequence Number
            # We use a Lock (sequence_counter) to ensure no two threads grab the same number.
            
            if self.sequence_counter and self.sequence_value:
                with self.sequence_counter:
                    seq_num = self.sequence_value[0]
                    self.sequence_value[0] += 1
            else:
                seq_num = 0  # Fallback path when sequence bookkeeping is disabled
            
            # STEP 2: Create Immutable Work Item
            item = WorkItem(item_id, sequence_number=seq_num)
            
            # STEP 3: Add to Shared Buffer (Critical Synchronization Point)
            # BLOCKS if full (Internally calls not_full.wait() to release lock)
            # The Producer thread will pause here if the Consumer is too slow.
            self.shared_queue.put(item)
            
            # Logging state for debugging
            buffer_after = self.shared_queue.qsize()
            buffer_before = buffer_after - 1  # Derive the before-state while we hold the slot
            # Pad single-digit IDs for nicer log alignment
            spacing = "   " if item_id < 10 else "  "
            self.logger.info(
                f"Produced WorkItem(id={item.item_id}){spacing}|  Buffer: {buffer_before} -> {buffer_after}"
            )
        
        self.logger.info("Finished production")
        # Note: This producer is done, but the Consumers might still be working.
        # We don't stop them here; the Manager handles that coordination.

class Consumer(threading.Thread):
    """Drains WorkItems from the shared queue into a private buffer."""
    
    def __init__(self, shared_queue: queue.Queue, name: str = "Consumer"):
        """
        Initialize the Consumer thread.
        
        Args:
            shared_queue: The common buffer to read WorkItems from.
            name: Identifier for logs (e.g., "Consumer-1").
        """
        # Initialize the parent thread to set up threading machinery
        super().__init__(name=name)
        
        self.shared_queue = shared_queue
        self.local_destination: List[WorkItem] = []  # Local buffer keeps locking simple
        self.logger = logging.getLogger(__name__)
    
    def get_destination(self) -> List[WorkItem]:
        """Expose the items consumed by this thread."""
        return self.local_destination

    def run(self) -> None:
        """
        The main execution loop for the Consumer thread.
        
        This loop runs indefinitely until it receives a specific 'Sentinel' (STOP_SIGNAL).
        
        Workflow:
        1. Wait for an item in the queue (Blocking).
        2. Check if it is the STOP_SIGNAL.
        3. If real item, process it (store in local list).
        4. Signal the queue that work is done (task_done).
        """
        self.logger.info("Starting")
        while True:
            # STEP 1: Retrieve Item (Blocking)
            # BLOCKS if empty (Internally calls not_empty.wait() to release lock)
            # The thread sleeps here if the Producer is slower than the Consumer.
            item = self.shared_queue.get()
            
            buffer_after = self.shared_queue.qsize()
            buffer_before = buffer_after + 1  # Capture the before-state while we own the slot
            
            # STEP 2: Check for Sentinel (Termination Condition)
            if item is STOP_SIGNAL:
                self.logger.info(f"Received STOP_SIGNAL. Quitting.  |  Buffer: {buffer_before} -> {buffer_after}")
                self.shared_queue.task_done()
                break
            
            # STEP 3: Process Item
            # In a real app, this is where expensive calculation would happen.
            # Here, we just store it to verify correctness later.
            self.local_destination.append(item)
            
            # Logging
            spacing = "   " if item.item_id < 10 else "  "
            self.logger.info(
                f"Consumed WorkItem(id={item.item_id}){spacing}|  Buffer: {buffer_before} -> {buffer_after}"
            )
            
            # STEP 4: Acknowledge Processing
            # Important for .join() on the queue to work correctly (though we don't use queue.join() here explicitly)
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
        """
        Slice the source data into balanced chunks for the producers.
        
        Example: 
            Items=10, Producers=3
            Returns: [[1, 2, 3, 4], [5, 6, 7], [8, 9, 10]]
        
        This ensures work is distributed as evenly as possible (load balancing).
        """
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
        """
        Execute the full simulation lifecycle.
        
        Phases:
        1. Setup: Reset state, initialize queues and buffers.
        2. Launch: Create and start N Producers and M Consumers.
        3. Monitor Producers: Wait for all producers to finish generating items.
        4. Signal Shutdown: Inject 'Sentinels' (STOP_SIGNAL) for consumers.
        5. Monitor Consumers: Wait for consumers to process remaining items and stop.
        6. Aggregation: Merge results from all consumers and sort by sequence.
        """
        # Reset mutable state so the manager can be reused safely
        self.sequence_value[0] = 0
        self.destination_data.clear()
        self.queue = queue.Queue(maxsize=self.queue_capacity)

        # PHASE 1: Create Threads
        # -----------------------
        
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
        
        # PHASE 2: Start Threads
        # ----------------------
        # Once started, they run independently in parallel.
        for producer in producers:
            producer.start()
        for consumer in consumers:
            consumer.start()
        
        # PHASE 3: Wait for Production
        # ----------------------------
        # We block here until every producer has finished its assigned chunk.
        for producer in producers:
            producer.join()
        
        # PHASE 4: Initiate Shutdown (Sentinel)
        # ----------------------------------------
        # Post one STOP signal per consumer. This is safer than checking "queue empty"
        # because the queue might be momentarily empty while producers are still working.
        # Since producers are joined (finished), we know no real items are coming after this.
        for _ in range(self.num_consumers):
            self.queue.put(STOP_SIGNAL)
        
        # PHASE 5: Wait for Consumption
        # -----------------------------
        # Consumers will process the remaining buffer, hit the STOP_SIGNAL, and exit.
        for consumer in consumers:
            consumer.join()
        
        # PHASE 6: Aggregate Results
        # --------------------------
        # Merge each consumer's private buffer
        all_results = []
        for consumer in consumers:
            all_results.extend(consumer.get_destination())
        
        # Sequence numbers restore the original ordering regardless of interleaving
        all_results.sort(key=lambda item: item.sequence_number)

        self.destination_data.extend(all_results)
        
        return self.destination_data

