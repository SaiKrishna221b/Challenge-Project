"""Assignment 1: Producer-Consumer Pattern Implementation - Entry Point."""

import sys
import os

# Ensure project root is on sys.path when executed as a module
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from ProducerConsumer.cli import main

if __name__ == "__main__":
    sys.exit(main())
