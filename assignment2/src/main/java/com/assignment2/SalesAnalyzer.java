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

    // ==========================================
    // 1. BASIC AGGREGATIONS
    // ==========================================

    /**
     * Calculates total sales revenue across all records.
     */
    public double calculateTotalSales() {
        return sales.stream()
                .mapToDouble(Sale::getTotalAmount)
                .sum();
    }

    // ==========================================
    // 2. REGIONAL ANALYSIS
    // ==========================================

    /**
     * Counts the number of sales transactions per region.
     */
    public Map<String, Long> getOrderCountByRegion() {
        return sales.stream()
                .collect(Collectors.groupingBy(
                        Sale::region,
                        Collectors.counting()
                ));
    }

    /**
     * NEW: Calculates the average order value (revenue per transaction) for each region.
     */
    public Map<String, Double> getAverageSalesByRegion() {
        return sales.stream()
                .collect(Collectors.groupingBy(
                        Sale::region,
                        Collectors.averagingDouble(Sale::getTotalAmount)
                ));
    }

    /**
     * NEW: Calculates the percentage of total revenue contributed by each region.
     */
    public Map<String, Double> getRevenuePercentageByRegion() {
        double totalRevenue = calculateTotalSales();
        if (totalRevenue == 0) return new HashMap<>();

        Map<String, Double> regionRevenue = sales.stream()
                .collect(Collectors.groupingBy(
                        Sale::region,
                        Collectors.summingDouble(Sale::getTotalAmount)
                ));
        
        // Convert raw totals to percentages
        return regionRevenue.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> (e.getValue() / totalRevenue) * 100.0
                ));
    }

    /**
     * NEW: Counts how many distinct product types are sold in each region.
     */
    public Map<String, Long> getDistinctProductCountByRegion() {
        return sales.stream()
                .collect(Collectors.groupingBy(
                        Sale::region,
                        Collectors.mapping(Sale::product, Collectors.collectingAndThen(Collectors.toSet(), set -> (long) set.size()))
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
     */
    public Map<String, DoubleSummaryStatistics> getSalesStatisticsByCategory() {
        return sales.stream()
                .collect(Collectors.groupingBy(
                        Sale::category,
                        Collectors.summarizingDouble(Sale::getTotalAmount)
                ));
    }

    /**
     * NEW: Returns a set of unique products available in each category.
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
     */
    public Optional<Map.Entry<String, Integer>> getTopProductByQuantity() {
        return sales.stream()
                .collect(Collectors.groupingBy(
                        Sale::product,
                        Collectors.summingInt(Sale::quantity)
                ))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue());
    }

    /**
     * NEW: Finds the top N products based on total revenue generated.
     * @param limit Number of top products to return
     */
    public List<Map.Entry<String, Double>> getTopProductsByRevenue(int limit) {
        return sales.stream()
                .collect(Collectors.groupingBy(
                        Sale::product,
                        Collectors.summingDouble(Sale::getTotalAmount)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
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
                        LinkedHashMap::new, // Preserve order if possible (though hash map doesn't guarantee insertion order of keys, dates often sort naturally if processed in order)
                        Collectors.summingDouble(Sale::getTotalAmount)
                ));
        // Note: To guarantee sorted order, we'd normally use a TreeMap, but LinkedHashMap is fine for this scope.
    }

    /**
     * NEW: Aggregates sales by Year.
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
     * True = High Value, False = Low Value.
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
                Iterator<String> lines = reader.lines().skip(1).iterator(); // Skip header
                
                List<Sale> batch = new ArrayList<>(chunkSize);
                int chunkId = 1;
                
                while (lines.hasNext()) {
                    String line = lines.next();
                    if (line.trim().isEmpty()) continue;
                    
                    batch.add(parseLine(line));
                    
                    if (batch.size() >= chunkSize) {
                        if (processBatchWithRetry(batch, chunkId)) {
                            totalRecords += batch.size();
                            
                            // Aggregate Batch Results into Global State
                            globalTotalRevenue += batch.stream().mapToDouble(Sale::getTotalAmount).sum();
                            
                            batch.forEach(s -> {
                                categoryRevenue.merge(s.category(), s.getTotalAmount(), Double::sum);
                                regionCounts.merge(s.region(), 1L, Long::sum);
                                productQuantity.merge(s.product(), s.quantity(), Integer::sum);
                                monthlyTrend.merge(s.getMonthYear(), s.getTotalAmount(), Double::sum);
                                
                                // Category Statistics (Total Amount)
                                categoryTotalStats.computeIfAbsent(s.category(), k -> new DoubleSummaryStatistics()).accept(s.getTotalAmount());
                                
                                // Category Statistics (Unit Price for Average)
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
                        batch.clear();
                        chunkId++;
                    }
                }
                
                // Process remaining
                if (!batch.isEmpty()) {
                     if (processBatchWithRetry(batch, chunkId)) {
                         totalRecords += batch.size();
                         // Aggregate Remaining Batch
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

        System.out.println("\n=====================================");
        System.out.println("       SALES DATA ANALYSIS");
        System.out.println("=====================================");

        // 1. Total Sales
        System.out.println("\n1. Total Sales Revenue:");
        System.out.printf("   $%.2f%n", globalTotalRevenue);

        // 2. Sales by Category
        System.out.println("\n2. Total Sales by Category:");
        categoryRevenue.forEach((category, revenue) -> 
            System.out.printf("   %s: $%.2f%n", category, revenue));

        // 3. Top Product
        System.out.println("\n3. Top Selling Product (by Quantity):");
        productQuantity.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .ifPresent(entry -> System.out.println("   " + entry.getKey() + " (" + entry.getValue() + " units)"));

        // 4. Region Count
        System.out.println("\n4. Orders by Region:");
        regionCounts.forEach((region, count) -> 
            System.out.println("   " + region + ": " + count + " orders"));

        // 5. Average Sales (Unit Price)
        System.out.println("\n5. Average Unit Price by Category:");
        categoryPriceStats.forEach((category, stats) -> 
            System.out.printf("   %s: $%.2f%n", category, stats.getAverage()));

        // 6. Highest Order
        System.out.println("\n6. Highest Value Order:");
        if (highestValueOrder != null) {
            System.out.printf("   ID: %s | Product: %s | Amount: $%.2f%n", 
                highestValueOrder.transactionId(), highestValueOrder.product(), highestValueOrder.getTotalAmount());
        }

        // 7. Advanced Statistics
        System.out.println("\n7. Detailed Statistics by Category:");
        categoryTotalStats.forEach((category, stats) -> {
            System.out.println("   " + category + ":");
            System.out.printf("      Count: %d, Min: $%.2f, Max: $%.2f, Avg: $%.2f%n", 
                stats.getCount(), stats.getMin(), stats.getMax(), stats.getAverage());
        });

        // 8. Monthly Trend
        System.out.println("\n8. Monthly Sales Trend:");
        monthlyTrend.forEach((month, total) -> 
            System.out.printf("   %s: $%.2f%n", month, total));

        System.out.println("\n=============================================");
        System.out.println("          END OF SALES REPORT");
        System.out.println("=============================================");
    }

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
