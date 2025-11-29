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

        // 3. Top Product (Quantity)
        System.out.println("\n3. Top Selling Product (by Quantity):");
        analyzer.getTopProductByQuantity().ifPresent(entry -> 
            System.out.println("   " + entry.getKey() + " (" + entry.getValue() + " units)"));

        // 4. Top 5 Products (Revenue) [NEW]
        System.out.println("\n4. Top 5 Products (by Revenue):");
        analyzer.getTopProductsByRevenue(5).forEach(entry -> 
            System.out.printf("   %s: $%.2f%n", entry.getKey(), entry.getValue()));

        // 5. Region Analysis (Count & Average) [ENHANCED]
        System.out.println("\n5. Regional Analysis:");
        System.out.println("   Order Counts:");
        analyzer.getOrderCountByRegion().forEach((region, count) -> 
            System.out.println("      " + region + ": " + count));
        
        System.out.println("   Average Order Value:");
        analyzer.getAverageSalesByRegion().forEach((region, avg) -> 
            System.out.printf("      %s: $%.2f%n", region, avg));

        // 6. Revenue Percentage by Region [NEW]
        System.out.println("   Revenue Share (%):");
        analyzer.getRevenuePercentageByRegion().forEach((region, pct) -> 
            System.out.printf("      %s: %.2f%%%n", region, pct));

        // 7. Unique Products by Region [NEW]
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
            System.out.printf("      Count: %d, Min: $%.2f, Max: $%.2f, Avg: $%.2f%n", 
                stats.getCount(), stats.getMin(), stats.getMax(), stats.getAverage());
        });
        
        // 11. Unique Products List [NEW]
        System.out.println("\n9. Unique Products per Category:");
        analyzer.getUniqueProductsByCategory().forEach((category, products) -> 
            System.out.println("   " + category + ": " + products));

        // 12. Monthly Trend
        System.out.println("\n10. Monthly Sales Trend:");
        analyzer.getMonthlySalesTrend().forEach((month, total) -> 
            System.out.printf("   %s: $%.2f%n", month, total));

        // 13. Yearly Sales [NEW]
        System.out.println("\n11. Yearly Sales:");
        analyzer.getYearlySales().forEach((year, total) -> 
            System.out.printf("   %d: $%.2f%n", year, total));

        System.out.println("\n=============================================");
        System.out.println("          END OF SALES REPORT");
        System.out.println("=============================================");
    }
}
