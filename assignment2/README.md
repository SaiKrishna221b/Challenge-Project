# Assignment 2: Sales Data Analysis with Java Streams

## Overview
This application performs data analysis on a CSV dataset of sales transactions. It demonstrates proficiency with **Java Streams**, **Functional Programming**, and **Data Aggregation** techniques. The program reads a CSV file, parses it into objects, and executes various analytical queries such as calculating total revenue, finding top-selling products, and grouping sales by region and category.

## Features & Stream Operations
The solution implements the following functional operations:
*   **Filtering & Mapping**: Parsing raw CSV lines into `Sale` objects (`map`), filtering empty lines.
*   **Aggregation**: Calculating total revenue (`mapToDouble`, `sum`).
*   **Grouping**: Grouping data by Category or Region (`collect`, `groupingBy`).
*   **Statistical Analysis**: Finding averages and maximums (`averagingDouble`, `max`).
*   **Sorting**: Finding top elements using Comparators (`sorted`, `max`).

## Project Structure
```
assignment2/
├── pom.xml                  # Maven build configuration
├── README.md                # This file
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/assignment2/
│   │   │       ├── Main.java            # Entry point (Console output)
│   │   │       ├── Sale.java            # Immutable Data Model (Java Record)
│   │   │       └── SalesAnalyzer.java   # Core Logic (Stream operations)
│   │   └── resources/
│   │       └── sales_data.csv       # Dataset
│   └── test/
│       ├── README.md                # Test documentation
│       └── java/
│           └── com/assignment2/
│               └── SalesAnalyzerTest.java # JUnit 5 Tests
```

## Setup & Execution

### Prerequisites
*   Java 17 or higher

### Option 1: Running with Maven (Recommended)
1.  Navigate to the `assignment2` directory:
    ```bash
    cd assignment2
    ```
2.  Compile and run:
    ```bash
    mvn clean compile exec:java "-Dexec.mainClass=com.assignment2.Main"
    ```
3.  Run tests:
    ```bash
    mvn test
    ```

### Option 4: Running with Docker (Zero Dependencies)
This ensures the application runs in a completely isolated environment. The Docker build process automatically executes all unit tests before creating the image.

1.  **Build the Docker Image**:
    ```bash
    cd assignment2
    docker build -t sales-analyzer .
    ```
    *(Note: This step runs `mvn test` internally. If tests fail, the build will fail.)*

2.  **Run the Application**:
    ```bash
    docker run --rm sales-analyzer
    ```

## Design Choices & Assumptions

### 1. Data Representation: Java Records
I chose **Java Records** (`record Sale(...)`) for the data model because:
*   **Immutability**: Fits the functional programming paradigm perfectly.
*   **Conciseness**: Eliminates boilerplate (getters, setters, constructors, `toString`, `equals`).
*   **Readability**: Makes the data structure clear and simple.

### 2. CSV Parsing: Native Streams
Instead of using an external library like OpenCSV, I implemented a custom parser using `BufferedReader.lines()` and Java Streams.
*   **Reasoning**: This explicitly fulfills the requirement to demonstrate "Stream operations" and "Functional programming" even during the data loading phase.
*   **Assumption**: The CSV format is simple (no commas inside fields). Code handles empty trailing lines gracefully.

### 3. Dataset Selection
I constructed a custom `sales_data.csv` containing 20 transactions with fields: `TransactionId`, `Date`, `Category`, `Product`, `Region`, `Quantity`, `UnitPrice`.
*   **Why**: This schema supports multi-dimensional aggregation (Category, Region, Time) and numerical analysis (Revenue, Count, Averages), allowing for a rich demonstration of Stream capabilities.

## Sample Output
```text
--- Loading Sales Data ---
Loaded 20 sales records.

--- Analysis Results ---
Total Revenue: $9582.00

Revenue by Category:
  Clothing: $767.00
  Electronics: $7975.00
  Home: $840.00

Top Selling Product (by Quantity):
  Socks (20 units)

Sales Count by Region:
  West: 5 transactions
  South: 5 transactions
  North: 5 transactions
  East: 5 transactions

Average Unit Price by Category:
  Clothing: $37.00
  Electronics: $514.38
  Home: $93.33

Most Expensive Sale Transaction:
  ID: T008 | Product: Laptop | Amount: $2400.00
```
