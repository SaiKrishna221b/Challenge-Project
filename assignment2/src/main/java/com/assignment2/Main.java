package com.assignment2;

import java.io.IOException;
import java.util.Scanner;

/**
 * Main entry point to demonstrate the SalesAnalyzer.
 */
public class Main {
    public static void main(String[] args) {
        // Determine execution mode: 1 = Standard (Memory), 2 = Batch (Chunking)
        int mode = 1; 
        int chunkSize = 1000;

        // 1. Check Command Line Arguments first (CI/Automation priority)
        if (args.length > 0) {
            try {
                chunkSize = Integer.parseInt(args[0]);
                mode = 2; // Argument implies Chunk Mode
            } catch (NumberFormatException e) {
                System.err.println("Invalid argument. Defaulting to Standard Mode.");
            }
        } 
        // 2. Interactive Mode (User Input)
        else {
            // Only prompt if we have a console (not piped input)
            if (System.console() != null || System.in != null) {
                try {
                    Scanner scanner = new Scanner(System.in);
                    System.out.println("Select Execution Mode:");
                    System.out.println("  [1] Standard Analysis (Load all data into memory - Stream API)");
                    System.out.println("  [2] Advanced Batch Processing (Chunking Engine - Scalable)");
                    System.out.print("Enter choice (1 or 2): ");
                    
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
                    // Fallback to default if input fails
                    System.out.println("Input error. Defaulting to Standard Mode.");
                }
            }
        }

        try {
            if (mode == 2) {
                // Run the ADVANCED Chunk Processing mode
                SalesAnalyzer.processInChunks("sales_data.csv", chunkSize);
            } else {
                // Standard In-Memory Analysis (Original Requirement)
                runStandardAnalysis();
            }

        } catch (IOException e) {
            System.err.println("Error reading data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void runStandardAnalysis() throws IOException {
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
    }
}
