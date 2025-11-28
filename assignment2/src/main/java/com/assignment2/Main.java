package com.assignment2;

import java.io.IOException;
import java.util.DoubleSummaryStatistics;

/**
 * Main entry point to demonstrate the SalesAnalyzer.
 */
public class Main {
    public static void main(String[] args) {
        try {
            // Check for optional Chunk Size argument (e.g., "java Main 1000")
            // If present, switches mode to "Streaming/Batch Processing" to handle large files.
            if (args.length > 0) {
                try {
                    int chunkSize = Integer.parseInt(args[0]);
                    // Run the ADVANCED Chunk Processing mode
                    SalesAnalyzer.processInChunks("sales_data.csv", chunkSize);
                    return; // Exit after chunk processing to avoid running standard mode
                } catch (NumberFormatException e) {
                    System.err.println("Invalid chunk size provided. Running standard analysis.");
                }
            }

            // Standard In-Memory Analysis (Original Requirement)
            System.out.println("--- Loading Sales Data ---");
            SalesAnalyzer analyzer = SalesAnalyzer.fromCsv("sales_data.csv");
            System.out.println("Loaded " + analyzer.getSales().size() + " sales records.");
            System.out.println();

            System.out.println("=====================================");
            System.out.println("       SALES DATA ANALYSIS");
            System.out.println("=====================================");

            // 1. Total Sales
            System.out.println("\n1. Total Sales Revenue:");
            System.out.printf("   $%.2f%n", analyzer.calculateTotalSales());

            // 2. Sales by Category
            System.out.println("\n2. Total Sales by Category:");
            analyzer.getSalesByCategory().forEach((category, revenue) -> 
                System.out.printf("   %s: $%.2f%n", category, revenue));

            // 3. Top Product
            System.out.println("\n3. Top Selling Product (by Quantity):");
            analyzer.getTopProductByQuantity().ifPresent(entry -> 
                System.out.println("   " + entry.getKey() + " (" + entry.getValue() + " units)"));

            // 4. Region Count
            System.out.println("\n4. Orders by Region:");
            analyzer.getOrderCountByRegion().forEach((region, count) -> 
                System.out.println("   " + region + ": " + count + " orders"));

            // 5. Average Sales
            System.out.println("\n5. Average Unit Price by Category:");
            analyzer.getAverageSalesByCategory().forEach((category, avgPrice) -> 
                System.out.printf("   %s: $%.2f%n", category, avgPrice));

            // 6. Highest Order
            System.out.println("\n6. Highest Value Order:");
            analyzer.getHighestValueOrder().ifPresent(sale -> 
                System.out.printf("   ID: %s | Product: %s | Amount: $%.2f%n", 
                    sale.transactionId(), sale.product(), sale.getTotalAmount()));

                // 7. Advanced Statistics
                System.out.println("\n7. Detailed Statistics by Category:");
                analyzer.getSalesStatisticsByCategory().forEach((category, stats) -> {
                    System.out.println("   " + category + ":");
                    System.out.printf("      Count: %d, Min: $%.2f, Max: $%.2f, Avg: $%.2f%n", 
                        stats.getCount(), stats.getMin(), stats.getMax(), stats.getAverage());
                });

                // 8. Monthly Trend
                System.out.println("\n8. Monthly Sales Trend:");
                analyzer.getMonthlySalesTrend().forEach((month, total) -> 
                    System.out.printf("   %s: $%.2f%n", month, total));

            System.out.println("\n=============================================");
            System.out.println("          END OF SALES REPORT");
            System.out.println("=============================================");

        } catch (IOException e) {
            System.err.println("Error reading data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
