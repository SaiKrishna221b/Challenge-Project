"""Utility functions for logging configuration."""

import logging
import sys
from typing import List

class LogCaptureHandler(logging.Handler):
    """Custom handler that captures log records with timestamps (silent capture)."""
    
    def __init__(self):
        super().__init__()
        self.captured_logs: List[logging.LogRecord] = []
    
    def emit(self, record: logging.LogRecord) -> None:
        """Capture log record silently (no console output)."""
        # Only capture, don't emit to console
        self.captured_logs.append(record)
    
    def get_sorted_logs(self) -> List[logging.LogRecord]:
        """Return captured logs sorted by timestamp."""
        return sorted(self.captured_logs, key=lambda r: r.created)
    
    def clear_logs(self) -> None:
        """Clear all captured logs."""
        self.captured_logs.clear()

def setup_logging() -> logging.Handler:
    """
    Configure root logger for multi-threaded simulation (silent capture only).
    
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

