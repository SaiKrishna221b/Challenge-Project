"""Command-line interface for the producer-consumer simulation."""

import argparse
import logging
import os
import sys
from datetime import datetime
from .core import SimulationManager
from .utils import setup_logging

def parse_args():
    """Wire up the CLI switches for batch-friendly runs."""
    parser = argparse.ArgumentParser(
        description="Producer-Consumer Simulation - Demonstrates thread synchronization",
        epilog="""
        Examples:
  python ProducerConsumer/main.py --items 100 --capacity 10 --producers 2 --consumers 3
  python ProducerConsumer/main.py  # Interactive mode
        """
    )
    parser.add_argument("--items", type=int, help="Number of items to produce (allowed range: 1-100000)")
    parser.add_argument("--capacity", type=int, help="Capacity of the shared queue (allowed range: 1-10000)")
    parser.add_argument("--producers", type=int, help="Number of producer threads (allowed range: 1-100)")
    parser.add_argument("--consumers", type=int, help="Number of consumer threads (allowed range: 1-100)")
    parser.add_argument("--save-logs", action="store_true", help="Automatically save simulation logs to a dated .txt file")
    return parser.parse_args()

def get_valid_input(prompt: str, min_value: int = 1, max_value: int = None) -> int:
    """Prompt until we receive an integer inside the allowed range.
    
    Args:
        prompt: The prompt string to display
        min_value: Minimum allowed value (default: 1)
        max_value: Maximum allowed value (None for no upper limit)
    
    Returns:
        Valid integer within the specified range
    """
    if not sys.stdin.isatty():
        raise EOFError("Interactive input not available. Use: --items <number> --capacity <number>")
    
    # Build range string for prompt
    if max_value is not None:
        range_str = f" (Allowed range: {min_value}-{max_value})"
    else:
        range_str = f" (Allowed range: {min_value} or greater)"
    
    full_prompt = prompt + range_str + ": "
    
    while True:
        try:
            value = int(input(full_prompt))
            if value < min_value:
                print(f"Error: Value must be at least {min_value}.")
            elif max_value is not None and value > max_value:
                print(f"Error: Value must be at most {max_value}.")
            else:
                return value
        except (ValueError, EOFError, KeyboardInterrupt):
            if isinstance(sys.exc_info()[1], EOFError):
                raise
            print("Error: Invalid input. Please enter a number.")

def display_timestamp_ordered_logs(log_handler) -> None:
    """Print captured logs in chronological order for easier reading."""
    sorted_logs = log_handler.get_sorted_logs()
    
    formatter = logging.Formatter("[%(threadName)s] - %(levelname)s - %(message)s")
    for record in sorted_logs:
        print(formatter.format(record))


def maybe_save_logs(log_handler, *, auto_save: bool = False) -> None:
    """Persist the current log stream to disk if requested."""
    if not auto_save and not sys.stdin.isatty():
        return

    if not auto_save:
        try:
            response = input("\nDo you want to save this simulation log? (y/n): ").strip().lower()
        except (EOFError, KeyboardInterrupt):
            print("\nSkipping save request.")
            return

        if response not in ("y", "yes"):
            return

    logs_dir = os.path.join(os.getcwd(), "simulation_logs")
    os.makedirs(logs_dir, exist_ok=True)

    now = datetime.now()
    dated_dir = os.path.join(logs_dir, str(now.year).zfill(4), str(now.month).zfill(2))
    os.makedirs(dated_dir, exist_ok=True)

    filename = f"{now.day:02d}.txt"
    filepath = os.path.join(dated_dir, filename)

    formatter = logging.Formatter("[%(threadName)s] - %(levelname)s - %(message)s")
    with open(filepath, "w", encoding="utf-8") as logfile:
        for record in log_handler.get_sorted_logs():
            logfile.write(formatter.format(record) + os.linesep)

    print(f"File stored successfully in folder {dated_dir}")

def run_simulation():
    """Run a single simulation iteration."""
    # Replace any existing handlers so we have a clean capture for this run
    root_logger = logging.getLogger()
    for handler in root_logger.handlers[:]:
        root_logger.removeHandler(handler)
    
    log_handler = setup_logging()
    logger = logging.getLogger("Main")
    
    print("=" * 60)
    print("Producer-Consumer Simulation")
    print("=" * 60)
    print()
    print("Welcome to the Producer-Consumer Pattern Demonstration!")
    print()
    print("This simulation demonstrates concurrent programming with multiple")
    print("producer and consumer threads working together:")
    print()
    print("  • Producer Threads: Create work items and add them to a")
    print("    shared buffer (queue)")
    print("  • Consumer Threads: Retrieve work items from the buffer")
    print("    and process them")
    print()
    print("The output shows real-time chronological order of events between threads.")
    print("Watch how the buffer state changes as items are produced")
    print("and consumed concurrently!")
    print()
    print("-" * 60)
    print()
    
    args = parse_args()
    
    if args.items is not None and args.capacity is not None:
        n_items = args.items
        capacity = args.capacity
        num_producers = args.producers if args.producers is not None else 1
        num_consumers = args.consumers if args.consumers is not None else 1
        
        if n_items < 1 or n_items > 100000:
            print(f"Error: Number of items must be in range 1-100000, got {n_items}")
            return False
        if capacity < 1 or capacity > 10000:
            print(f"Error: Queue capacity must be in range 1-10000, got {capacity}")
            return False
        if num_producers < 1 or num_producers > 100:
            print(f"Error: Number of producers must be in range 1-100, got {num_producers}")
            return False
        if num_consumers < 1 or num_consumers > 100:
            print(f"Error: Number of consumers must be in range 1-100, got {num_consumers}")
            return False
    else:
        try:
            n_items = get_valid_input("Enter number of items to produce", min_value=1, max_value=100000)
            capacity = get_valid_input("Enter queue capacity", min_value=1, max_value=10000)
            num_producers = get_valid_input("Enter number of producer threads", min_value=1, max_value=100)
            num_consumers = get_valid_input("Enter number of consumer threads", min_value=1, max_value=100)
        except EOFError as e:
            print(f"\nError: {e}")
            print("\nUsage examples:")
            print("  python ProducerConsumer/main.py --items 20 --capacity 5 --producers 2 --consumers 3")
            return False

    print()
    print("Logs")
    print("====")
    print()
    print(f"Buffer capacity: {capacity}")
    print(f"Number of producers: {num_producers}")
    print(f"Number of consumers: {num_consumers}")
    print()
    
    logger.info(f"Starting simulation with {n_items} items, capacity {capacity}, "
                f"{num_producers} producers, {num_consumers} consumers")

    try:
        manager = SimulationManager(n_items, capacity, num_producers, num_consumers)
    except (ValueError, TypeError) as e:
        print(f"Error: Invalid input - {e}")
        logger.error(f"Validation error: {e}")
        return False
    
    try:
        results = manager.run()
    except Exception as e:
        print(f"Error: Simulation failed - {e}")
        logger.exception("Simulation error occurred")
        return False

    # Display timestamp-ordered logs first
    display_timestamp_ordered_logs(log_handler)
    
    print()
    print("=== Simulation Complete ===")
    print(f"Items Produced: {n_items}")
    print(f"Items Consumed: {len(results)}")
    
    if len(results) == n_items:
        print("SUCCESS: All items processed successfully.")
    else:
        print("FAILURE: Item count mismatch!")
        logger.warning(f"Item count mismatch: expected {n_items}, got {len(results)}")

    maybe_save_logs(log_handler, auto_save=args.save_logs)
    
    return True

def main():
    """Main entry point with run-again loop."""
    try:
        while True:
            success = run_simulation()
            
            if not sys.stdin.isatty():
                # Non-interactive shell (e.g., CLI flags or CI) should exit immediately
                return 0 if success else 1
            
            # Ask if user wants to run again
            try:
                response = input("\nRun simulation again? (y/n): ").strip().lower()
                if response not in ('y', 'yes'):
                    print("Exiting. Thank you!")
                    break
                print()  # Add spacing before the next iteration
            except (EOFError, KeyboardInterrupt):
                print("\nExiting. Thank you!")
                break
                
        return 0
            
    except KeyboardInterrupt:
        print("\n\nSimulation interrupted by user.")
        return 130
    except Exception as e:
        print(f"Unexpected error: {e}")
        return 1

if __name__ == "__main__":
    sys.exit(main())

