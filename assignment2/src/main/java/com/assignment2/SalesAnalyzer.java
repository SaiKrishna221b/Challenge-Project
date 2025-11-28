package com.assignment2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Analyzes sales data using Java Streams and functional programming.
 */
public class SalesAnalyzer {

    private final List<Sale> sales;

    public SalesAnalyzer(List<Sale> sales) {
        this.sales = new ArrayList<>(sales);
    }

    /**
     * Loads sales data from a CSV file in the classpath resources.
     * Uses Streams for reading and parsing.
     *
     * @param filename Name of the CSV file in resources
     * @return SalesAnalyzer instance populated with data
     * @throws IOException if file cannot be read
     */
    public static SalesAnalyzer fromCsv(String filename) throws IOException {
        try (InputStream is = SalesAnalyzer.class.getClassLoader().getResourceAsStream(filename)) {
            if (is == null) {
                throw new IOException("File not found in resources: " + filename);
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                List<Sale> salesList = reader.lines()
                        .skip(1) // Skip header
                        .filter(line -> !line.trim().isEmpty()) // Skip empty lines
                        .map(SalesAnalyzer::parseLine)
                        .collect(Collectors.toList());
                return new SalesAnalyzer(salesList);
            }
        }
    }

    /**
     * Parses a single CSV line into a Sale object.
     * Assumes format: TransactionId,Date,Category,Product,Region,Quantity,UnitPrice
     */
    private static Sale parseLine(String line) {
        String[] parts = line.split(",");
        // Basic validation/parsing logic
        return new Sale(
            parts[0].trim(),
            LocalDate.parse(parts[1].trim()),
            parts[2].trim(),
            parts[3].trim(),
            parts[4].trim(),
            Integer.parseInt(parts[5].trim()),
            Double.parseDouble(parts[6].trim())
        );
    }

    public List<Sale> getSales() {
        return new ArrayList<>(sales);
    }

    // --- Analytical Methods using Streams & Aggregation ---

    /**
     * Calculates total revenue across all sales.
     * Demonstrates: mapToDouble, sum
     */
    public double calculateTotalRevenue() {
        return sales.stream()
                .mapToDouble(Sale::getTotalAmount)
                .sum();
    }

    /**
     * Groups sales by Category and calculates total revenue for each.
     * Demonstrates: collect, groupingBy, summingDouble
     */
    public Map<String, Double> getRevenueByCategory() {
        return sales.stream()
                .collect(Collectors.groupingBy(
                        Sale::category,
                        Collectors.summingDouble(Sale::getTotalAmount)
                ));
    }

    /**
     * Finds the top selling product based on quantity sold.
     * Demonstrates: collect, groupingBy, summingInt, max Map.Entry
     */
    public Optional<Map.Entry<String, Integer>> getTopSellingProduct() {
        return sales.stream()
                .collect(Collectors.groupingBy(
                        Sale::product,
                        Collectors.summingInt(Sale::quantity)
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue());
    }

    /**
     * Counts the number of sales transactions per region.
     * Demonstrates: collect, groupingBy, counting
     */
    public Map<String, Long> getSalesCountByRegion() {
        return sales.stream()
                .collect(Collectors.groupingBy(
                        Sale::region,
                        Collectors.counting()
                ));
    }

    /**
     * Calculates the average unit price for each category.
     * Demonstrates: collect, groupingBy, averagingDouble
     */
    public Map<String, Double> getAveragePriceByCategory() {
        return sales.stream()
                .collect(Collectors.groupingBy(
                        Sale::category,
                        Collectors.averagingDouble(Sale::unitPrice)
                ));
    }

    /**
     * Finds the most expensive sale (highest total amount).
     * Demonstrates: max, Comparator
     */
    public Optional<Sale> getMostExpensiveSale() {
        return sales.stream()
                .max(Comparator.comparingDouble(Sale::getTotalAmount));
    }
    
    /**
     * ADVANCED: Processes the file in chunks to handle large datasets.
     * 
     * Why this exists:
     * Large files cannot fit in memory (List<Sale>). We read them in blocks (Chunks).
     * 
     * Logic Flow:
     * 1. Read file line-by-line (lazy stream).
     * 2. Accumulate lines into a small 'batch' List.
     * 3. When batch is full, 'process' it (update global stats).
     * 4. If processing fails, retry (Fault Tolerance).
     * 
     * @param filename The resource filename
     * @param chunkSize Number of records to process in memory at once
     */
    public static void processInChunks(String filename, int chunkSize) throws IOException {
        System.out.println("--- Starting Chunked Processing (Size: " + chunkSize + ") ---");
        
        // Accumulators for Global State
        double globalTotalRevenue = 0;
        Map<String, Long> globalRegionCounts = new HashMap<>();
        int totalChunks = 0;
        int failedChunks = 0;
        
        try (InputStream is = SalesAnalyzer.class.getClassLoader().getResourceAsStream(filename)) {
            if (is == null) throw new IOException("File not found: " + filename);
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                Iterator<String> lines = reader.lines().skip(1).iterator(); // Skip header
                
                List<Sale> batch = new ArrayList<>(chunkSize);
                int chunkId = 1;
                
                while (lines.hasNext()) {
                    String line = lines.next();
                    if (line.trim().isEmpty()) continue;
                    
                    batch.add(parseLine(line));
                    
                    if (batch.size() >= chunkSize) {
                        boolean success = processBatchWithRetry(batch, chunkId, globalTotalRevenue, globalRegionCounts);
                        if (success) {
                            globalTotalRevenue += batch.stream().mapToDouble(Sale::getTotalAmount).sum();
                            batch.forEach(s -> globalRegionCounts.merge(s.region(), 1L, Long::sum));
                        } else {
                            failedChunks++;
                        }
                        totalChunks++;
                        batch.clear();
                        chunkId++;
                    }
                }
                
                // Process remaining
                if (!batch.isEmpty()) {
                     boolean success = processBatchWithRetry(batch, chunkId, globalTotalRevenue, globalRegionCounts);
                     if (success) {
                         globalTotalRevenue += batch.stream().mapToDouble(Sale::getTotalAmount).sum();
                         batch.forEach(s -> globalRegionCounts.merge(s.region(), 1L, Long::sum));
                     } else {
                         failedChunks++;
                     }
                     totalChunks++;
                }
            }
        }
        
        System.out.println("\n--- Processing Summary ---");
        if (failedChunks == 0) {
            System.out.println("Status: SUCCESS");
            System.out.println("Processed " + totalChunks + " chunks without error.");
        } else {
            System.err.println("Status: PARTIAL FAILURE");
            System.err.println("Processed " + totalChunks + " chunks. Failed: " + failedChunks);
        }

        System.out.println("\n--- Final Aggregated Results ---");
        System.out.printf("Global Total Revenue: $%.2f%n", globalTotalRevenue);
        System.out.println("Global Region Counts: " + globalRegionCounts);
    }

    private static boolean processBatchWithRetry(List<Sale> batch, int chunkId, double currentRev, Map<String, Long> currentRegions) {
        int retries = 3;
        while (retries > 0) {
            try {
                System.out.printf("Processing Chunk #%d (%d records)... ", chunkId, batch.size());
                // Simulate processing logic
                // if (chunkId == 2 && retries == 3) throw new RuntimeException("Simulated Network Glitch");
                
                System.out.println("SUCCESS");
                return true;
            } catch (Exception e) {
                retries--;
                System.err.println("FAILED (" + e.getMessage() + "). Retrying... " + retries + " attempts left.");
                if (retries == 0) {
                    System.err.println("CRITICAL: Chunk #" + chunkId + " failed permanently. Skipping.");
                    return false;
                }
            }
        }
        return false;
    }
}
