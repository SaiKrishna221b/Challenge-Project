#Intuit Build Challenge

## Assignment 1 – Producer-Consumer Pattern

The Assignment 1 implementation lives under `assignment1/ProducerConsumer` and ships with a CLI plus a comprehensive pytest suite. Follow the steps below to run it locally.

Producer-Consumer **Version 1** accepts *Item Number* (`--items`) and *Queue Capacity* (`--capacity`) as configurable inputs to the simulation.

### 1. Prerequisites
- Python 3.11+

### 2. Setup
```bash
# From the project root
cd assignment1

# (Optional but recommended)
python -m venv .venv
.venv\Scripts\activate      # Windows
source .venv/bin/activate   # macOS/Linux

pip install -r requirements.txt
```

### 3. Run the Simulation
```bash
python -m ProducerConsumer.main --items 100 --capacity 10
```
- Use `--items` to control how many work items the producer generates.
- Use `--capacity` to set the bounded buffer size.
- Omitting the flags launches interactive mode and prompts for both values.

### 4. Run Tests
```bash
python -m pytest tests -q
```

For deeper documentation (architecture, logging, advanced scenarios), see `assignment1/README.md`.

