# Source Code Documentation

This package `com.assignment2` contains the core implementation of the Sales Analysis application.

## Files Description

### 1. `Sale.java` (Data Model)
*   **Type**: Java Record (Immutable).
*   **Purpose**: Represents a single row from the CSV file.
*   **Fields**: `transactionId`, `date`, `category`, `product`, `region`, `quantity`, `unitPrice`.
*   **Methods**: `getTotalAmount()` calculates the total transaction value (`quantity * unitPrice`).

### 2. `SalesAnalyzer.java` (Core Logic)
*   **Type**: Class.
*   **Purpose**: Handles data loading and performs functional stream operations.
*   **Key Methods**:
    *   `fromCsv(String filename)`: Static factory method that reads the CSV using `BufferedReader.lines()` and streams.
    *   `calculateTotalRevenue()`: Uses `mapToDouble().sum()`.
    *   `getRevenueByCategory()`: Uses `Collectors.groupingBy` and `summingDouble`.
    *   `getTopSellingProduct()`: Finds max element based on grouped sums.
    *   `getSalesCountByRegion()`: Counts occurrences per region.
    *   `getAveragePriceByCategory()`: Calculates averages.
    *   **`processInChunks(String filename, int chunkSize)`**: Advanced method that reads the file in blocks to simulate large-scale batch processing with retry logic.

### 3. `Main.java` (Entry Point)
*   **Type**: Class.
*   **Purpose**: Driver class to execute the application from the console.
*   **Flow**:
    1.  Checks for optional CLI argument (Chunk Size).
    2.  If argument exists, runs **Chunked Processing**.
    3.  If no argument, runs **Standard In-Memory Analysis**.
    4.  Formats and prints results to `System.out`.
