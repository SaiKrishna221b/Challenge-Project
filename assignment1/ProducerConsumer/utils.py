"""Logging helpers shared by the CLI and tests."""

import logging
from typing import List

class LogCaptureHandler(logging.Handler):
    """In-memory handler we can query later for timestamp-ordered logs."""
    
    def __init__(self):
        super().__init__()
        self.captured_logs: List[logging.LogRecord] = []
    
    def emit(self, record: logging.LogRecord) -> None:
        """Capture log records without emitting them anywhere else."""
        self.captured_logs.append(record)
    
    def get_sorted_logs(self) -> List[logging.LogRecord]:
        """Return captured logs sorted by timestamp."""
        return sorted(self.captured_logs, key=lambda r: r.created)
    
    def clear_logs(self) -> None:
        """Clear all captured logs."""
        self.captured_logs.clear()

def setup_logging() -> logging.Handler:
    """
    Configure the root logger for the simulation so every thread writes to the same buffer.
    
    Returns:
        LogCaptureHandler instance for accessing captured logs
    """
    capture_handler = LogCaptureHandler()
    
    logging.basicConfig(
        level=logging.INFO,
        handlers=[capture_handler],
        format="[%(threadName)s] - %(levelname)s - %(message)s"
    )
    
    return capture_handler

