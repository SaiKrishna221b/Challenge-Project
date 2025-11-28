# Intuit Build Challenge

This repository contains my solutions for the Intuit Build Challenge.

### Quick Links
*   [Replication Steps](#quick-start-guide)
*   [Project Structure](#project-structure)
*   [Results: Producer Consumer v2.1](#assignment-1-producer-consumer-pattern)
*   [Results: Sales Analyzer v2.0](#sample-output-assignment-2)

---

## Assignment 1: Producer-Consumer Pattern
**Path:** `assignment1/ProducerConsumer`

A robust, thread-safe implementation of the classic synchronization problem, evolved through multiple iterations.

### Version History

#### **v1.0: The Core Implementation**
*   **Bounded Buffer**: Implemented a thread-safe queue using `threading.Lock` and `Condition` variables through Queue.queue methods.
*   **Basic Synchronization**: Handled `wait()` and `notify()` logic to prevent race conditions.
*   **Interactive CLI**: Prompts users for `items` and `capacity` if arguments are missing.
*   **CI Pipeline**: Automated testing via GitHub Actions.

#### **v2.0: Enhanced Concurrency**
*   **Multi-Threading Support**: Scaled to support $N$ Producers and $M$ Consumers running simultaneously.
*   **CLI Arguments**: Added `--producers` and `--consumers` flags for granular control.
*   **Sequence Tracking**: Implemented global sequence numbering to verify data integrity across threads.

#### **v2.1: DevOps & Robustness**
*   **Dockerization**: Full container support for isolated execution.
*   **Structured Logging**: Thread-aware logs for debugging complex interleaving and saved output file.

---

## Assignment 2: Sales Data Analysis
**Path:** `assignment2`

A high-performance data analysis tool leveraging Java Streams and Functional Programming paradigms.

### Version History

#### **v1.0: Functional MVP**
*   **Stream API**: Replaced imperative loops with declarative Stream pipelines (`map`, `filter`, `collect`).
*   **Core Metrics**: Implemented 6 essential aggregations:
    *   Total Sales Revenue
    *   Revenue by Category
    *   Top Selling Product
    *   Order Count by Region
    *   Average Unit Price
    *   Highest Value Order
*   **Immutable Data**: Used Java Records (`record Sale`) to enforce immutability.

#### **v2.0: Enterprise Scale & Analytics**
*   **Advanced Analytics**:
    *   **Statistical Profiles**: Uses `DoubleSummaryStatistics` for single-pass calculation of Min/Max/Avg/Sum.
    *   **Temporal Trends**: Aggregates sales by Month (`YYYY-MM`) for time-series analysis.
    *   **Partitioning**: Segments data into High/Low value tiers.
*   **Scale Architecture**:
    *   **Chunk Processing Engine**: Implemented a lazy-loading batch processor to handle datasets larger than RAM (verified with 20,000+ rows).
    *   **Fault Tolerance**: Built-in retry logic for failed chunks.
  

---

## Quick Start Guide

### Assignment 1 (Python)
```bash
cd assignment1
pip install -r requirements.txt
# Run the full multi-threaded simulation
python -m ProducerConsumer.main --items 50 --capacity 5 --producers 2 --consumers 2
```

### Assignment 2 (Java)
```bash
cd assignment2
# Build and Run using Maven
mvn clean compile exec:java "-Dexec.mainClass=com.assignment2.Main"

# OR Run the Advanced Chunking Engine (Batch Size 1000)
mvn clean compile exec:java "-Dexec.mainClass=com.assignment2.Main" "-Dexec.args=1000"
```

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

*   **Python Workflow**: Triggers on changes to `assignment1/`. Runs `pytest`.
*   **Java Workflow**: Triggers on changes to `assignment2/`. Runs `mvn test`.
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
│   └── Dockerfile                          # Container definition
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
