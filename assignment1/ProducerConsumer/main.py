"""ProducerConsumer pattern entry point."""

import sys
import os

# Ensure package imports resolve when executed as a script
sys.path.insert(0, os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from ProducerConsumer.cli import main

if __name__ == "__main__":
    sys.exit(main())
