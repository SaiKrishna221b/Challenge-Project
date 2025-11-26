# Assignment 1: Producer-Consumer Pattern Implementation

## Overview

This assignment implements the classic producer-consumer pattern demonstrating thread synchronization, concurrent programming, blocking queues, and wait/notify mechanisms.

## Structure

```
ProducerConsumer/
├── __init__.py      # Package initialization
├── main.py          # Entry point
├── core.py          # Core implementation (Producer, Consumer, SimulationManager)
├── models.py        # Data models (WorkItem)
├── cli.py           # Command-line interface
├── utils.py         # Utility functions (logging setup)
└── README.md        # This file
```

## Components

### Core Classes

- **`Producer`**: Thread that creates WorkItems and places them in a shared queue
- **`Consumer`**: Thread that retrieves WorkItems from the shared queue
- **`SimulationManager`**: Orchestrates producer and consumer threads

### Data Model

- **`WorkItem`**: Immutable work item (thread-safe by design)

## Usage

### Command-Line Mode

```bash
python -m ProducerConsumer.main --items 100 --capacity 10
```

### Interactive Mode

```bash
python -m ProducerConsumer.main
```

## Thread Synchronization

The implementation uses `queue.Queue`, which internally employs `threading.Condition` for wait/notify synchronization:

- **Producer blocks** when queue is full (waits on 'not_full' condition)
- **Consumer blocks** when queue is empty (waits on 'not_empty' condition)
- Automatic notification between threads ensures efficient coordination

## Testing

Run tests with:

```bash
pytest tests/test_functional.py
pytest tests/test_performance.py
```

