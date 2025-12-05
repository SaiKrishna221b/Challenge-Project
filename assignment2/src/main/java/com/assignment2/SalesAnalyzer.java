package com.assignment2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Analyzes sales data using Java Streams and functional programming.
 * 
 * CONCEPT: Functional Programming
 * This class demonstrates the declarative style of programming where we describe *what* we want
 * (filter, map, reduce) rather than *how* to do it (for-loops, if-statements).
 */
public class SalesAnalyzer {

    // Immutable list of sales for in-memory analysis
    private final List<Sale> sales;

    public SalesAnalyzer(List<Sale> sales) {
        this.sales = new ArrayList<>(sales); // Defensive copy to ensure encapsulation
    }

    /**
     * Factory method to load sales data from a CSV file.
     * 
     * CONCEPT: Try-With-Resources
     * The syntax 'try (Resource r = ...)' ensures that the resource is automatically closed
     * at the end of the block, preventing memory leaks.
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
            // CONCEPT: Streams for I/O
            // BufferedReader.lines() creates a Stream<String> that lazily reads the file.
            // This is efficient because it doesn't load the whole file into memory at once
            // until the terminal operation (.collect) is called.
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                List<Sale> salesList = reader.lines()
                        .skip(1) // Skip the CSV header row
                        .filter(line -> !line.trim().isEmpty()) // Filter out empty lines (Robustness)
                        .map(SalesAnalyzer::parseLine) // Transform: String -> Sale object
                        .collect(Collectors.toList()); // Terminal Op: Gather results into a List
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
        // Real-world app would add more error handling here (e.g., check array length)
        return new Sale(
            parts[0].trim(),
            LocalDate.parse(parts[1].trim()), // Parses YYYY-MM-DD format automatically
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

    // ==========================================
    // 1. BASIC AGGREGATIONS
    // ==========================================

    /**
     * Calculates total sales revenue across all records.
     * 
     * CONCEPT: Map-Reduce Pattern
     * 1. mapToDouble: Transform Stream<Sale> to DoubleStream (extracts amount)
     * 2. sum: Reduces the stream to a single value by adding them up
     */
    public double calculateTotalSales() {
        return sales.stream()
                .mapToDouble(Sale::getTotalAmount) // Method Reference: clearer than lambda (s -> s.getTotalAmount())
                .sum();
    }

    // ==========================================
    // 2. REGIONAL ANALYSIS
    // ==========================================

    /**
     * Counts the number of sales transactions per region.
     * 
     * CONCEPT: Collectors.groupingBy
     * Equivalent to SQL: SELECT Region, COUNT(*) FROM Sales GROUP BY Region
     */
    public Map<String, Long> getOrderCountByRegion() {
        return sales.stream()
                .collect(Collectors.groupingBy(
                        Sale::region,        // The Key (Group by Region)
                        Collectors.counting() // The Value (Count items in group)
                ));
    }

    /**
     * Calculates the average order value (revenue per transaction) for each region.
     */
    public Map<String, Double> getAverageSalesByRegion() {
        return sales.stream()
                .collect(Collectors.groupingBy(
                        Sale::region,
                        Collectors.averagingDouble(Sale::getTotalAmount)
                ));
    }

    /**
     * Calculates the percentage of total revenue contributed by each region.
     */
    public Map<String, Double> getRevenuePercentageByRegion() {
        double totalRevenue = calculateTotalSales();
        if (totalRevenue == 0) return new HashMap<>();

        // First, calculate total revenue per region
        Map<String, Double> regionRevenue = sales.stream()
                .collect(Collectors.groupingBy(
                        Sale::region,
                        Collectors.summingDouble(Sale::getTotalAmount)
                ));
        
        // Then, transform those totals into percentages
        // We create a new stream from the Map's entries to process them
        return regionRevenue.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> (e.getValue() / totalRevenue) * 100.0
                ));
    }

    /**
     * Counts how many distinct product types are sold in each region.
     * 
     * CONCEPT: Downstream Collectors
     * groupingBy takes a second argument (downstream collector) to process the values in the group.
     * Here we map to product name -> collect to Set (unique) -> get size.
     */
    public Map<String, Long> getDistinctProductCountByRegion() {
        return sales.stream()
                .collect(Collectors.groupingBy(
                        Sale::region,
                        Collectors.mapping(
                            Sale::product, 
                            Collectors.collectingAndThen(Collectors.toSet(), set -> (long) set.size())
                        )
                ));
    }

    // ==========================================
    // 3. CATEGORY ANALYSIS
    // ==========================================

    /**
     * Groups sales by Category and calculates total revenue for each.
     */
    public Map<String, Double> getSalesByCategory() {
        return sales.stream()
                .collect(Collectors.groupingBy(
                        Sale::category,
                        Collectors.summingDouble(Sale::getTotalAmount)
                ));
    }

    /**
     * Calculates the average unit price for each category.
     */
    public Map<String, Double> getAverageSalesByCategory() {
        return sales.stream()
                .collect(Collectors.groupingBy(
                        Sale::category,
                        Collectors.averagingDouble(Sale::unitPrice)
                ));
    }

    /**
     * Advanced: Calculates comprehensive statistics (Min, Max, Avg, Sum, Count) per category.
     * 
     * CONCEPT: DoubleSummaryStatistics
     * This is a high-performance collector that computes count, sum, min, max, and average
     * in a SINGLE pass over the data. Much more efficient than iterating 5 times.
     */
    public Map<String, DoubleSummaryStatistics> getSalesStatisticsByCategory() {
        return sales.stream()
                .collect(Collectors.groupingBy(
                        Sale::category,
                        Collectors.summarizingDouble(Sale::getTotalAmount)
                ));
    }

    /**
     * Returns a set of unique products available in each category.
     */
    public Map<String, Set<String>> getUniqueProductsByCategory() {
        return sales.stream()
                .collect(Collectors.groupingBy(
                        Sale::category,
                        Collectors.mapping(Sale::product, Collectors.toSet())
                ));
    }

    // ==========================================
    // 4. PRODUCT ANALYSIS
    // ==========================================

    /**
     * Finds the top selling product based on quantity sold.
     * 
     * CONCEPT: Optional
     * The result is wrapped in an 'Optional' because the list might be empty.
     * This forces the caller to handle the "no result" case, preventing NullPointerExceptions.
     */
    public Optional<Map.Entry<String, Integer>> getTopProductByQuantity() {
        return sales.stream()
                // 1. Calculate total quantity per product
                .collect(Collectors.groupingBy(
                        Sale::product,
                        Collectors.summingInt(Sale::quantity)
                ))
                // 2. Stream the results (Map.Entry<Product, Quantity>)
                .entrySet().stream()
                // 3. Find the maximum by value
                .max(Map.Entry.comparingByValue());
    }

    /**
     * Finds the top N products based on total revenue generated.
     * @param limit Number of top products to return
     */
    public List<Map.Entry<String, Double>> getTopProductsByRevenue(int limit) {
        return sales.stream()
                .collect(Collectors.groupingBy(
                        Sale::product,
                        Collectors.summingDouble(Sale::getTotalAmount)
                ))
                .entrySet().stream()
                // Sort descending by value (Revenue)
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit) // Take only the top N
                .collect(Collectors.toList());
    }

    // ==========================================
    // 5. TEMPORAL ANALYSIS
    // ==========================================

    /**
     * Aggregates sales by Month (YYYY-MM).
     */
    public Map<String, Double> getMonthlySalesTrend() {
        return sales.stream()
                .collect(Collectors.groupingBy(
                        Sale::getMonthYear,
                        LinkedHashMap::new, // Use LinkedHashMap to preserve insertion order (useful if dates are sorted)
                        Collectors.summingDouble(Sale::getTotalAmount)
                ));
    }

    /**
     * Aggregates sales by Year.
     */
    public Map<Integer, Double> getYearlySales() {
        return sales.stream()
                .collect(Collectors.groupingBy(
                        Sale::getYear,
                        Collectors.summingDouble(Sale::getTotalAmount)
                ));
    }

    // ==========================================
    // 6. ADVANCED ANALYSIS
    // ==========================================

    /**
     * Finds the single highest value order.
     */
    public Optional<Sale> getHighestValueOrder() {
        return sales.stream()
                .max(Comparator.comparingDouble(Sale::getTotalAmount));
    }

    /**
     * Partitions orders into "High Value" and "Low Value" based on a threshold.
     * 
     * CONCEPT: partitioningBy
     * A special case of groupingBy where the key is a boolean.
     * Returns a Map with exactly two keys: true and false.
     */
    public Map<Boolean, List<Sale>> partitionOrdersByValue(double threshold) {
        return sales.stream()
                .collect(Collectors.partitioningBy(
                        sale -> sale.getTotalAmount() >= threshold
                ));
    }
    
    // ==========================================
    // 7. SCALE / CHUNKING LOGIC
    // ==========================================

    /**
     * ADVANCED: Processes the file in chunks to handle large datasets.
     * 
     * SCALABILITY PATTERN:
     * Large files (GBs/TBs) cannot fit in memory (List<Sale>). 
     * We read them in blocks (Chunks) to keep memory usage constant (O(1)).
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
        
        // Global Accumulators (State needs to be maintained across chunks)
        double globalTotalRevenue = 0;
        Map<String, Double> categoryRevenue = new HashMap<>();
        Map<String, Long> regionCounts = new HashMap<>();
        Map<String, Integer> productQuantity = new HashMap<>();
        Map<String, DoubleSummaryStatistics> categoryTotalStats = new HashMap<>();
        Map<String, DoubleSummaryStatistics> categoryPriceStats = new HashMap<>();
        Map<String, Double> monthlyTrend = new HashMap<>();
        Sale highestValueOrder = null;

        int totalChunks = 0;
        int failedChunks = 0;
        int totalRecords = 0;
        
        try (InputStream is = SalesAnalyzer.class.getClassLoader().getResourceAsStream(filename)) {
            if (is == null) throw new IOException("File not found: " + filename);
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                // Iterator allows us to pull lines one by one from the Stream
                Iterator<String> lines = reader.lines().skip(1).iterator(); // Skip header
                
                List<Sale> batch = new ArrayList<>(chunkSize);
                int chunkId = 1;
                
                while (lines.hasNext()) {
                    String line = lines.next();
                    if (line.trim().isEmpty()) continue;
                    
                    batch.add(parseLine(line));
                    
                    // When batch is full, process it
                    if (batch.size() >= chunkSize) {
                        if (processBatchWithRetry(batch, chunkId)) {
                            totalRecords += batch.size();
                            
                            // --- Manual Aggregation Step ---
                            // Since we processed a chunk, we must manually merge its results into the global maps.
                            // Unlike simple Streams, we manage state explicitly here.
                            
                            globalTotalRevenue += batch.stream().mapToDouble(Sale::getTotalAmount).sum();
                            
                            batch.forEach(s -> {
                                // Map.merge is atomic and clean for updating counts/sums
                                categoryRevenue.merge(s.category(), s.getTotalAmount(), Double::sum);
                                regionCounts.merge(s.region(), 1L, Long::sum);
                                productQuantity.merge(s.product(), s.quantity(), Integer::sum);
                                monthlyTrend.merge(s.getMonthYear(), s.getTotalAmount(), Double::sum);
                                
                                // Complex stats objects need computeIfAbsent
                                categoryTotalStats.computeIfAbsent(s.category(), k -> new DoubleSummaryStatistics()).accept(s.getTotalAmount());
                                categoryPriceStats.computeIfAbsent(s.category(), k -> new DoubleSummaryStatistics()).accept(s.unitPrice());
                            });

                            // Track Highest Order
                            Optional<Sale> batchHigh = batch.stream().max(Comparator.comparingDouble(Sale::getTotalAmount));
                            if (batchHigh.isPresent()) {
                                if (highestValueOrder == null || batchHigh.get().getTotalAmount() > highestValueOrder.getTotalAmount()) {
                                    highestValueOrder = batchHigh.get();
                                }
                            }

                        } else {
                            failedChunks++;
                        }
                        totalChunks++;
                        batch.clear(); // Reset for next chunk
                        chunkId++;
                    }
                }
                
                // Don't forget the last partial batch!
                if (!batch.isEmpty()) {
                     if (processBatchWithRetry(batch, chunkId)) {
                         totalRecords += batch.size();
                         // (Repeat aggregation logic for final batch)
                         globalTotalRevenue += batch.stream().mapToDouble(Sale::getTotalAmount).sum();
                         batch.forEach(s -> {
                             categoryRevenue.merge(s.category(), s.getTotalAmount(), Double::sum);
                             regionCounts.merge(s.region(), 1L, Long::sum);
                             productQuantity.merge(s.product(), s.quantity(), Integer::sum);
                             monthlyTrend.merge(s.getMonthYear(), s.getTotalAmount(), Double::sum);
                             categoryTotalStats.computeIfAbsent(s.category(), k -> new DoubleSummaryStatistics()).accept(s.getTotalAmount());
                             categoryPriceStats.computeIfAbsent(s.category(), k -> new DoubleSummaryStatistics()).accept(s.unitPrice());
                         });

                         Optional<Sale> batchHigh = batch.stream().max(Comparator.comparingDouble(Sale::getTotalAmount));
                         if (batchHigh.isPresent()) {
                             if (highestValueOrder == null || batchHigh.get().getTotalAmount() > highestValueOrder.getTotalAmount()) {
                                 highestValueOrder = batchHigh.get();
                             }
                         }
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
            System.out.println("Processed " + totalChunks + " chunks (" + totalRecords + " records) without error.");
        } else {
            System.err.println("Status: PARTIAL FAILURE");
            System.err.println("Processed " + totalChunks + " chunks. Failed: " + failedChunks);
        }

        // Print results (reuses standard output format)
        System.out.println("\n=====================================");
        System.out.println("       SALES DATA ANALYSIS");
        System.out.println("=====================================");

        System.out.println("\n1. Total Sales Revenue:");
        System.out.printf("   $%.2f%n", globalTotalRevenue);

        System.out.println("\n2. Total Sales by Category:");
        categoryRevenue.forEach((category, revenue) -> 
            System.out.printf("   %s: $%.2f%n", category, revenue));

        System.out.println("\n3. Top Selling Product (by Quantity):");
        productQuantity.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .ifPresent(entry -> System.out.println("   " + entry.getKey() + " (" + entry.getValue() + " units)"));

        System.out.println("\n4. Orders by Region:");
        regionCounts.forEach((region, count) -> 
            System.out.println("   " + region + ": " + count + " orders"));

        System.out.println("\n5. Average Unit Price by Category:");
        categoryPriceStats.forEach((category, stats) -> 
            System.out.printf("   %s: $%.2f%n", category, stats.getAverage()));

        System.out.println("\n6. Highest Value Order:");
        if (highestValueOrder != null) {
            System.out.printf("   ID: %s | Product: %s | Amount: $%.2f%n", 
                highestValueOrder.transactionId(), highestValueOrder.product(), highestValueOrder.getTotalAmount());
        }

        System.out.println("\n7. Detailed Statistics by Category:");
        categoryTotalStats.forEach((category, stats) -> {
            System.out.println("   " + category + ":");
            System.out.printf("      Count: %d, Min: $%.2f, Max: $%.2f, Avg: $%.2f%n", 
                stats.getCount(), stats.getMin(), stats.getMax(), stats.getAverage());
        });

        System.out.println("\n8. Monthly Sales Trend:");
        monthlyTrend.forEach((month, total) -> 
            System.out.printf("   %s: $%.2f%n", month, total));

        System.out.println("\n=============================================");
        System.out.println("          END OF SALES REPORT");
        System.out.println("=============================================");
    }

    /**
     * Helper to process a single batch with retry logic.
     * 
     * CONCEPT: Fault Tolerance
     * In distributed systems or large batch jobs, transient failures (network blips, I/O locks) happen.
     * Instead of crashing the whole job, we retry a few times.
     */
    private static boolean processBatchWithRetry(List<Sale> batch, int chunkId) {
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
