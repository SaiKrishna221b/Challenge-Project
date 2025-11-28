package com.assignment2;

import java.io.IOException;

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
            // Loads entire file into List<Sale>. Best for small datasets.
            System.out.println("--- Loading Sales Data ---");
            SalesAnalyzer analyzer = SalesAnalyzer.fromCsv("sales_data.csv");
            System.out.println("Loaded " + analyzer.getSales().size() + " sales records.");
            System.out.println();

            System.out.println("--- Analysis Results ---");

            // 1. Total Revenue
            double totalRevenue = analyzer.calculateTotalRevenue();
            System.out.printf("Total Revenue: $%.2f%n", totalRevenue);
            System.out.println();

            // 2. Revenue by Category
            System.out.println("Revenue by Category:");
            analyzer.getRevenueByCategory().forEach((category, revenue) -> 
                System.out.printf("  %s: $%.2f%n", category, revenue));
            System.out.println();

            // 3. Top Selling Product
            System.out.println("Top Selling Product (by Quantity):");
            analyzer.getTopSellingProduct().ifPresent(entry -> 
                System.out.println("  " + entry.getKey() + " (" + entry.getValue() + " units)"));
            System.out.println();

            // 4. Sales Count by Region
            System.out.println("Sales Count by Region:");
            analyzer.getSalesCountByRegion().forEach((region, count) -> 
                System.out.println("  " + region + ": " + count + " transactions"));
            System.out.println();

            // 5. Average Price by Category
            System.out.println("Average Unit Price by Category:");
            analyzer.getAveragePriceByCategory().forEach((category, avgPrice) -> 
                System.out.printf("  %s: $%.2f%n", category, avgPrice));
            System.out.println();

            // 6. Most Expensive Sale
            System.out.println("Most Expensive Sale Transaction:");
            analyzer.getMostExpensiveSale().ifPresent(sale -> 
                System.out.printf("  ID: %s | Product: %s | Amount: $%.2f%n", 
                    sale.transactionId(), sale.product(), sale.getTotalAmount()));

        } catch (IOException e) {
            System.err.println("Error reading data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
