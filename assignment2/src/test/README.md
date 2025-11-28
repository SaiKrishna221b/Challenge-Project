# Unit Testing Documentation

This directory contains the Unit Tests for the Sales Analysis application.

## Test File: `SalesAnalyzerTest.java`

This class uses **JUnit 5 (Jupiter)** to verify the correctness of the `SalesAnalyzer` methods.

### Test Coverage
The tests cover the following scenarios using a controlled in-memory dataset (List of 4 mock `Sale` objects):

1.  **`testCalculateTotalRevenue`**: Verifies that the sum of all sales is correct.
2.  **`testGetRevenueByCategory`**: Checks if sales are correctly grouped and summed by category (Electronics vs. Clothing).
3.  **`testGetTopSellingProduct`**: Verifies the logic for finding the product with the highest quantity.
4.  **`testGetSalesCountByRegion`**: Ensures the count of transactions per region is accurate.
5.  **`testGetAveragePriceByCategory`**: Checks the average calculation logic.
6.  **`testGetMostExpensiveSale`**: Verifies finding the maximum value element in the stream.
7.  **`testEmptyDataset`**: Ensures the application handles empty data gracefully without crashing.

## How to Run Tests

### Option 1: With Maven (Recommended)
Run the following command from the `assignment2` root directory:
```bash
mvn test
```

### Option 2: Manual Compilation (Requires JUnit JARs)
If Maven is not available, you must have `junit-platform-console-standalone.jar` on your classpath.
```bash
java -jar junit-platform-console-standalone.jar -cp bin --scan-classpath
```

