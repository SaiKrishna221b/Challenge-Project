# Intuit Build Challenge

This repository contains my original contributions for the 2 assignment questions in the Build Challenge (see instructions pdf in root folder)

---

## Assignment 1: Producer-Consumer Pattern
Located in `assignment1/ProducerConsumer`. Implements a thread-safe bounded buffer simulation.

### Features by Version

#### Version 1 (Baseline)
*   **Configurable Items**: Set the total number of items to produce (`--items`).
*   **Configurable Capacity**: Set the size of the bounded buffer (`--capacity`).
*   **Interactive Mode**: Prompts user for inputs if flags are omitted.
*   **Thread Safety**: Uses `threading.Lock` plus the `queue.Queue` condition variables to protect critical sections.

#### Version 2 (New Features)
*   **Multi-Producer/Consumer**: Support for multiple concurrent producer and consumer threads.
*   **New CLI Flags**:
    *   `--producers N`: Set number of producer threads.
    *   `--consumers N`: Set number of consumer threads.
*   **Enhanced Logging**: Tracks thread IDs to visualize concurrency.

#### Version 2.1 (Latest)
*   **Save Output Logs**: Use `--save-logs` or answer “y” when prompted to store simulation logs under `simulation_logs/YYYY/MM/DD.txt`.
*   **Expanded Test Coverage**: Added dedicated tests for log persistence and tightened concurrency edge-case coverage.

### Quick Start
```bash
cd assignment1
pip install -r requirements.txt

# Run Simulation
python -m ProducerConsumer.main --items 50 --capacity 5 --producers 2 --consumers 2

# Run Tests
python -m pytest tests
```

### Replication Steps (Docker)
To run without installing Python locally:
```bash
cd assignment1
docker build -t producer-consumer .
docker run -it --rm producer-consumer --items 50 --capacity 5
```

*For detailed documentation and screenshots, see `assignment1/README.md`.*

---

## Assignment 2: Sales Data Analysis
Located in `assignment2`. A Java application demonstrating functional programming and stream processing on CSV data.

### Overview
*   **Tech Stack**: Java 17+, Maven, JUnit 5.
*   **Key Concepts**: Java Records, Stream API (`map`, `reduce`, `collect`), Aggregation.
*   **Data**: Analyzes a dataset of 20,000+ sales records.

### Replication Steps

#### Option 1: Using Maven (Recommended)
Builds the project, runs unit tests, and executes the analysis.
```bash
cd assignment2
mvn clean compile exec:java "-Dexec.mainClass=com.assignment2.Main"
```
*To run with a specific chunk size (e.g., 1000):*
```bash
mvn clean compile exec:java "-Dexec.mainClass=com.assignment2.Main" "-Dexec.args=1000"
```

#### Option 2: Using Docker (Zero Dependencies)
Runs the application in an isolated container environment.
```bash
cd assignment2

# Build Image (Runs tests automatically)
docker build -t sales-analyzer .

# Run Container
docker run --rm sales-analyzer
```

*For design choices and full output samples, see `assignment2/README.md`.*

---

## Testing Strategies

### Assignment 1: Python Testing Suite (Pytest)
| Category | Description | Key Test Cases |
| :--- | :--- | :--- |
| **Functional** | Verifies core Producer-Consumer logic. | `test_basic_production_consumption` |
| **Concurrency** | Checks for race conditions and deadlocks. | `test_thread_safety`, `test_timeout` |
| **Data Integrity** | Ensures no items are lost or duplicated. | `test_sequence_ordering` |
| **Edge Cases** | Validates system stability under stress. | `test_zero_capacity`, `test_negative_input` |

### Assignment 2: Java Testing Suite (JUnit 5 & Manual)
| Method | Tool | Purpose |
| :--- | :--- | :--- |
| **Unit Testing** | **JUnit 5** | Verifies mathematical correctness of Stream aggregations (Sum, Avg, Grouping) using controlled mock data. |
| **Integration** | **Docker** | Builds and tests the entire application in an isolated container environment. |
| **Manual Verification** | **Script** | `ManualTest.java` runs a dependency-free health check of core logic without Maven. |

---

## Continuous Integration (CI)

The repository is configured with a **GitHub Actions** pipeline to ensure code quality on every push.

*   **Python Workflow**:
    *   Sets up Python 3.11 environment.
    *   Installs dependencies (`requirements.txt`).
    *   Runs the full `pytest` suite.
*   **Java Workflow**:
    *   Sets up JDK 17 environment.
    *   Runs `mvn clean test` to verify compilation and unit tests.
*   **Status**: ✅ Builds pass automatically on pull requests.

---

## Project Structure

```text
projectIntuit/
├── README.md                               # This file
├── assignment1/                            # Python Producer-Consumer Solution
│   ├── ProducerConsumer/
│   │   ├── main.py                         # Entry point for the CLI simulation
│   │   ├── core.py                         # Buffer logic (Producer, Consumer, BoundedBuffer classes)
│   │   ├── models.py                       # Data classes (WorkItem)
│   │   └── cli.py                          # Argument parsing logic
│   ├── tests/                              # Pytest suite (concurrency, edge cases, integration)
│   └── requirements.txt                    # Python dependencies (pytest)
│
└── assignment2/                            # Java Sales Analysis Solution
    ├── src/
    │   ├── main/java/com/assignment2/
    │   │   ├── Main.java                   # Entry point (CLI arguments, reporting)
    │   │   ├── SalesAnalyzer.java          # Core logic (Streams, Chunking, Aggregation)
    │   │   └── Sale.java                   # Immutable Data Model (Java Record)
    │   └── resources/
    │       └── sales_data.csv              # Dataset (20,000 rows)
    ├── src/test/java/com/assignment2/
    │   ├── SalesAnalyzerTest.java          # JUnit 5 Unit Tests
    │   └── ManualTest.java                 # Zero-dependency script for manual verification
    ├── pom.xml                             # Maven build configuration
    └── Dockerfile                          # Container definition for build/test/run
```
