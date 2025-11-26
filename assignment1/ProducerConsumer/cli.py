"""Command-line interface for the producer-consumer simulation."""

import argparse
import logging
import sys
from .core import SimulationManager
from .utils import setup_logging

def parse_args():
    """Parse command-line arguments."""
    parser = argparse.ArgumentParser(
        description="Producer-Consumer Simulation - Demonstrates thread synchronization",
        epilog="""
Examples:
  python ProducerConsumer/main.py --items 100 --capacity 10
  python ProducerConsumer/main.py  # Interactive mode
        """
    )
    parser.add_argument("--items", type=int, help="Number of items to produce (allowed range: 1-100000)")
    parser.add_argument("--capacity", type=int, help="Capacity of the shared queue (allowed range: 1-10000)")
    return parser.parse_args()

def get_valid_input(prompt: str, min_value: int = 1, max_value: int = None) -> int:
    """Get integer input interactively within specified range.
    
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
    """Display logs sorted by timestamp."""
    sorted_logs = log_handler.get_sorted_logs()
    
    formatter = logging.Formatter("[%(threadName)s] - %(levelname)s - %(message)s")
    for record in sorted_logs:
        print(formatter.format(record))

def run_simulation():
    """Run a single simulation iteration."""
    # Clear any existing log handlers and set up fresh
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
    print("This simulation demonstrates concurrent programming with two")
    print("threads working together:")
    print()
    print("  • Producer Thread: Creates work items and adds them to a")
    print("    shared buffer (queue)")
    print("  • Consumer Thread: Retrieves work items from the buffer")
    print("    and processes them")
    print()
    print("The output shows real-time synchronization between threads.")
    print("Watch how the buffer state changes as items are produced")
    print("and consumed concurrently!")
    print()
    print("-" * 60)
    print()
    
    args = parse_args()
    
    if args.items is not None and args.capacity is not None:
        n_items = args.items
        capacity = args.capacity
        
        if n_items < 1 or n_items > 100000:
            print(f"Error: Number of items must be in range 1-100000, got {n_items}")
            return False
        if capacity < 1 or capacity > 10000:
            print(f"Error: Queue capacity must be in range 1-10000, got {capacity}")
            return False
    else:
        try:
            n_items = get_valid_input("Enter number of items to produce", min_value=1, max_value=100000)
            capacity = get_valid_input("Enter queue capacity", min_value=1, max_value=10000)
        except EOFError as e:
            print(f"\nError: {e}")
            print("\nUsage examples:")
            print("  python ProducerConsumer/main.py --items 20 --capacity 5")
            return False

    print()
    print("Logs")
    print("====")
    print()
    print(f"Buffer capacity: {capacity}")
    print()
    
    logger.info(f"Starting simulation with {n_items} items and capacity {capacity}")

    try:
        manager = SimulationManager(n_items, capacity)
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
    
    return True

def main():
    """Main entry point with run-again loop."""
    try:
        while True:
            success = run_simulation()
            
            if not sys.stdin.isatty():
                # Non-interactive mode, exit after one run
                return 0 if success else 1
            
            # Ask if user wants to run again
            try:
                response = input("\nRun simulation again? (y/n): ").strip().lower()
                if response not in ('y', 'yes'):
                    print("Exiting. Thank you!")
                    break
                print()  # Add spacing before next run
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

