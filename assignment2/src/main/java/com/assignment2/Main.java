package com.assignment2;

import java.io.IOException;
import java.util.Scanner;

/**
 * Main entry point to demonstrate the SalesAnalyzer.
 * 
 * This class handles User Interaction (UI) and Control Flow.
 * It separates the "how to run" logic from the "how to analyze" logic (Separation of Concerns).
 */
public class Main {
    
    /**
     * The standard Java entry point.
     * 
     * @param args Command line arguments passed to the program. 
     *             We use this to support automation/CI environments where
     *             user input isn't possible.
     */
    public static void main(String[] args) {
        // Default configuration
        int mode = 1; // 1 = Standard (Memory), 2 = Batch (Chunking)
        int chunkSize = 1000;

        // -------------------------------------------------
        // 1. Command Line Argument Parsing (Priority 1)
        // -------------------------------------------------
        // If arguments exist, we assume an automated run (e.g., from a script or Docker).
        if (args.length > 0) {
            try {
                chunkSize = Integer.parseInt(args[0]);
                mode = 2; // Presence of an argument implies we want to test the Chunk Mode
                System.out.println("Command line argument detected. Running in Chunk Mode with size: " + chunkSize);
            } catch (NumberFormatException e) {
                System.err.println("Invalid argument provided (not a number). Defaulting to Standard Mode.");
            }
        } 
        // -------------------------------------------------
        // 2. Interactive Mode (Priority 2)
        // -------------------------------------------------
        // If no args, we try to ask the human user.
        else {
            // System.console() checks if we are attached to a real terminal.
            // This prevents crashes if run in environments without input (like some IDE background tasks).
            if (System.console() != null || System.in != null) {
                try {
                    // 'Scanner' is a text scanner that parses primitive types and strings.
                    // It wraps System.in (standard input stream).
                    Scanner scanner = new Scanner(System.in);
                    System.out.println("Select Execution Mode:");
                    System.out.println("  [1] Standard Analysis (Load all data into memory - Stream API)");
                    System.out.println("  [2] Advanced Batch Processing (Chunking Engine - Scalable)");
                    System.out.print("Enter choice (1 or 2): ");
                    
                    // defensive coding: check if input is actually an integer before reading
                    if (scanner.hasNextInt()) {
                        int choice = scanner.nextInt();
                        if (choice == 2) {
                            mode = 2;
                            System.out.print("Enter batch size (default 1000): ");
                            if (scanner.hasNextInt()) {
                                chunkSize = scanner.nextInt();
                            }
                        }
                    }
                } catch (Exception e) {
                    // Graceful degradation: If input fails, just run the default mode.
                    System.out.println("Input error. Defaulting to Standard Mode.");
                }
            }
        }

        // -------------------------------------------------
        // Execution Phase
        // -------------------------------------------------
        try {
            if (mode == 2) {
                // Run the ADVANCED Chunk Processing mode (Scalable solution)
                SalesAnalyzer.processInChunks("sales_data.csv", chunkSize);
            } else {
                // Run the Standard In-Memory Analysis (Original Assignment Requirement)
                runStandardAnalysis();
            }

        } catch (IOException e) {
            // IOException is a "Checked Exception". We must handle it or declare it.
            // Here, we catch it to print a user-friendly error message instead of crashing with a stack trace.
            System.err.println("CRITICAL ERROR: Could not read the data file.");
            System.err.println("Details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Executes the standard analysis suite using the Stream API.
     * 
     * KEYWORD: 'throws IOException'
     * This method declares that it might fail with an input/output error.
     * It delegates the responsibility of handling that error to the caller (main method).
     */
    private static void runStandardAnalysis() throws IOException {
        System.out.println("--- Loading Sales Data ---");
        
     
        SalesAnalyzer analyzer = SalesAnalyzer.fromCsv("sales_data.csv");
        
        System.out.println("Loaded " + analyzer.getSales().size() + " sales records.");
        System.out.println();

        System.out.println("=====================================");
        System.out.println("       SALES DATA ANALYSIS");
        System.out.println("=====================================");

        // -------------------------------------------------
        // REPORT GENERATION
        // -------------------------------------------------
        
        // 1. Total Sales
        System.out.println("\n1. Total Sales Revenue:");
        System.out.printf("   $%.2f%n", analyzer.calculateTotalSales());

        // 2. Sales by Category
        // Uses forEach with a BiConsumer (key, value) -> action
        System.out.println("\n2. Total Sales by Category:");
        analyzer.getSalesByCategory().forEach((category, revenue) -> 
            System.out.printf("   %s: $%.2f%n", category, revenue));

        // 3. Top Product (Quantity)
        System.out.println("\n3. Top Selling Product (by Quantity):");
        // Optional usage: ifPresent avoids null checks
        analyzer.getTopProductByQuantity().ifPresent(entry -> 
            System.out.println("   " + entry.getKey() + " (" + entry.getValue() + " units)"));

        // 4. Top 5 Products (Revenue)
        System.out.println("\n4. Top 5 Products (by Revenue):");
        analyzer.getTopProductsByRevenue(5).forEach(entry -> 
            System.out.printf("   %s: $%.2f%n", entry.getKey(), entry.getValue()));

        // 5. Region Analysis
        System.out.println("\n5. Regional Analysis:");
        System.out.println("   Order Counts:");
        analyzer.getOrderCountByRegion().forEach((region, count) -> 
            System.out.println("      " + region + ": " + count));
        
        System.out.println("   Average Order Value:");
        analyzer.getAverageSalesByRegion().forEach((region, avg) -> 
            System.out.printf("      %s: $%.2f%n", region, avg));

        // 6. Revenue Share
        System.out.println("   Revenue Share (%):");
        analyzer.getRevenuePercentageByRegion().forEach((region, pct) -> 
            System.out.printf("      %s: %.2f%%%n", region, pct));

        // 7. Distinct Products
        System.out.println("   Distinct Products Sold:");
        analyzer.getDistinctProductCountByRegion().forEach((region, count) -> 
            System.out.println("      " + region + ": " + count));

        // 8. Average Sales (Category)
        System.out.println("\n6. Average Unit Price by Category:");
        analyzer.getAverageSalesByCategory().forEach((category, avgPrice) -> 
            System.out.printf("   %s: $%.2f%n", category, avgPrice));

        // 9. Highest Order
        System.out.println("\n7. Highest Value Order:");
        analyzer.getHighestValueOrder().ifPresent(sale -> 
            System.out.printf("   ID: %s | Product: %s | Amount: $%.2f%n", 
                sale.transactionId(), sale.product(), sale.getTotalAmount()));

        // 10. Advanced Statistics
        System.out.println("\n8. Detailed Statistics by Category:");
        analyzer.getSalesStatisticsByCategory().forEach((category, stats) -> {
            System.out.println("   " + category + ":");
            // DoubleSummaryStatistics provides count, sum, min, max, and average in one object
            System.out.printf("      Count: %d, Min: $%.2f, Max: $%.2f, Avg: $%.2f%n", 
                stats.getCount(), stats.getMin(), stats.getMax(), stats.getAverage());
        });
        
        // 11. Unique Products
        System.out.println("\n9. Unique Products per Category:");
        analyzer.getUniqueProductsByCategory().forEach((category, products) -> 
            System.out.println("   " + category + ": " + products));

        // 12. Monthly Trend
        System.out.println("\n10. Monthly Sales Trend:");
        analyzer.getMonthlySalesTrend().forEach((month, total) -> 
            System.out.printf("   %s: $%.2f%n", month, total));

        // 13. Yearly Sales
        System.out.println("\n11. Yearly Sales:");
        analyzer.getYearlySales().forEach((year, total) -> 
            System.out.printf("   %d: $%.2f%n", year, total));

        System.out.println("\n=============================================");
        System.out.println("          END OF SALES REPORT");
        System.out.println("=============================================");
    }
}
