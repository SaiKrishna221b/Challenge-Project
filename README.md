# Intuit Build Challenge

This repository contains my original solutions for the Intuit Build Challenge. 

### Quick Links
*   [Replication Steps](#quick-start-guide)
*   [Project Structure](#project-structure)
*   [Design Decisions: Assignment 1](assignment1/DesignDecisions.md)
*   [Design Decisions: Assignment 2](assignment2/DesignDecisions.md)
*   [Results: Producer Consumer v2.1](#assignment-1-results-v21)
*   [Results: Sales Analyzer v3.0](#assignment-2-results-v30)

---

## Assignment 1: Producer-Consumer Pattern
**Path:** `assignment1/ProducerConsumer`
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

#### **v3.0: Parity & Polish**
*   **More Insights**: Added advanced aggregation metrics including:
    *   **Revenue Share**: Percentage of global revenue per region.
    *   **Unique Product Lists**: Distinct products sold per category.
    *   **Top N Ranking**: Flexible ranking by Revenue (not just Quantity).
*   **Interactive CLI**: Added a user-friendly menu to select between Standard and Scalable modes.
*   **Unified Reporting**: Ensured the Chunking Engine produces the exact same high-fidelity report as the Standard engine.

---

## Quick Start Guide

### Assignment 1 (Python)
```bash
cd assignment1
pip install -r requirements.txt
# Run the full multi-threaded simulation
python -m ProducerConsumer.main --items 50 --capacity 5 --producers 2 --consumers 2

# Docker Build & Run
docker build -t assignment1-python .
docker run -it assignment1-python
```

### Assignment 2 (Java)
```bash
cd assignment2
# Interactive Mode (Select Standard or Batch Processing)
mvn clean compile exec:java "-Dexec.mainClass=com.assignment2.Main"

# Docker Build & Run
docker build -t assignment2-java .
docker run assignment2-java
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

---

## Deliverables & Implementation

### Assignment 1: Producer-Consumer (Python)
| Requirement | Implementation File | Method / Strategy |
| :--- | :--- | :--- |
| **Complete Source Code** | `assignment1/ProducerConsumer/` | Core logic in `core.py`, CLI in `cli.py` |
| **Unit Tests** | `assignment1/tests/` | Full Pytest suite covering concurrency & data integrity |
| **Console Output** | `main.py` | Real-time logging to stdout + saved log files |
| **Thread Safety** | `core.py` | Uses `queue.Queue` (Condition/Lock based) |

### Assignment 2: Sales Analysis (Java)
| Requirement | Implementation File | Method / Strategy |
| :--- | :--- | :--- |
| **Complete Source Code** | `assignment2/src/main/java/` | `SalesAnalyzer.java`, `Sale.java` (Record), `Main.java` |
| **Unit Tests** | `assignment2/src/test/java/` | `SalesAnalyzerTest.java` (JUnit 5), `ChunkingTest.java` |
| **Functional Programming** | `SalesAnalyzer.java` | Pure functions, Immutability (`record Sale`) |
| **Stream Operations** | `SalesAnalyzer.java` | `.map()`, `.filter()`, `.reduce()`, `.collect()` pipelines |
| **Lambda Expressions** | `SalesAnalyzer.java` | `sale -> sale.getTotalAmount() >= threshold` |
| **Data Aggregation** | `SalesAnalyzer.java` | `Collectors.groupingBy`, `summarizingDouble`, `partitioningBy` |
| **EXTRA: Scalability** | `SalesAnalyzer.java` | **Chunking Engine**: Process infinite file sizes in O(1) memory |

---

## Assignment 1 Results (v2.1)

Here are the execution results from the **Producer-Consumer v2.1** implementation (Dockerized, Multi-threaded).

![Producer Consumer Output 1](Results/ProducerConsumer%20v2.1/v2.1%20ss1.png)
![Producer Consumer Output 2](Results/ProducerConsumer%20v2.1/v2.1%20ss2.png)

---

## Assignment 2 Results (v3.0)

Here are the execution results from the **Sales Analyzer v3.0** (Java Streams, Large Dataset).

![Sales Analyzer Output 1](Results/SalesAnalyzer%20v3/v3%20ss1.png)
![Sales Analyzer Output 2](Results/SalesAnalyzer%20v3/v3%20ss2.png)
![Sales Analyzer Output 3](Results/SalesAnalyzer%20v3/v3%20ss3.png)

<details>
<summary>Click to see full text output</summary>

```text
--- Loading Sales Data ---
Loaded 20000 sales records.

=====================================
       SALES DATA ANALYSIS
=====================================

1. Total Sales Revenue:
   $110,407,964.25

2. Total Sales by Category:
   Clothing: $22,685,861.13
   Electronics: $22,412,091.25
   Books: $21,924,422.47
   ...

3. Top Selling Product (by Quantity):
   Hat (4638 units)

4. Top 5 Products (by Revenue):
   Mouse: $4,709,488.86
   Socks: $4,690,871.48
   Hat: $4,617,463.77
   ...

5. Regional Analysis:
   Order Counts:
      West: 5042
      South: 5015
   Average Order Value:
      West: $5,489.81
      South: $5,507.06
   Revenue Share (%):
      West: 25.07%
      South: 25.01%

6. Average Unit Price by Category:
   Clothing: $1006.63
   Electronics: $1011.01
   ...

7. Highest Value Order:
   ID: T07864 | Product: Monitor | Amount: $19990.70

8. Detailed Statistics by Category:
   Clothing:
      Count: 4062, Min: $20.05, Max: $19985.10, Avg: $5584.90
   ...

9. Unique Products per Category:
   Clothing: [Jacket, T-Shirt, Jeans, Hat, Socks]
   Electronics: [Headphones, Laptop, Monitor, Mouse, Keyboard]
   ...

10. Monthly Sales Trend:
   2024-01: $9,494,647.62
   2024-02: $8,408,740.30
   ...

11. Yearly Sales:
   2024: $110,407,964.25

=============================================
          END OF SALES REPORT
=============================================
```
</details>
