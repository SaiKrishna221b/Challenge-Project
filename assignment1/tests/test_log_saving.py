"""Tests for log persistence behavior."""

import logging
import os
from datetime import datetime

import pytest

from ProducerConsumer.cli import maybe_save_logs
from ProducerConsumer.utils import LogCaptureHandler


class _FrozenDateTime(datetime):
    """Helper that returns a deterministic timestamp."""

    @classmethod
    def now(cls):
        return cls(2025, 11, 28, 15, 45, 30)


@pytest.fixture
def log_handler():
    handler = LogCaptureHandler()
    logger = logging.getLogger("test_log_saving")
    logger.addHandler(handler)
    logger.setLevel(logging.INFO)
    logger.info("Sample log entry")
    logger.removeHandler(handler)
    return handler


def test_auto_save_creates_dated_txt(tmp_path, monkeypatch, log_handler):
    """Auto-save should write logs to simulation_logs/YYYY/MM/DD.txt."""
    monkeypatch.chdir(tmp_path)
    monkeypatch.setattr("ProducerConsumer.cli.datetime", _FrozenDateTime)

    maybe_save_logs(log_handler, auto_save=True)

    expected_dir = tmp_path / "simulation_logs" / "2025" / "11"
    expected_file = expected_dir / "28.txt"

    assert expected_dir.is_dir(), "Year/month directory should be created"
    assert expected_file.is_file(), "Daily log file should exist"

    contents = expected_file.read_text(encoding="utf-8")
    assert "Sample log entry" in contents

