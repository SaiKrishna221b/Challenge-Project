package com.assignment2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SalesAnalyzerTest {

    private SalesAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        // Create a controlled dataset with Edge Cases
        List<Sale> testSales = Arrays.asList(
            // Standard Sales
            new Sale("T1", LocalDate.of(2024, 1, 1), "Electronics", "Laptop", "North", 1, 1000.00),
            new Sale("T2", LocalDate.of(2024, 1, 2), "Electronics", "Mouse", "South", 5, 20.00),
            new Sale("T3", LocalDate.of(2024, 2, 1), "Clothing", "T-Shirt", "North", 2, 50.00),
            new Sale("T4", LocalDate.of(2024, 2, 5), "Clothing", "Jeans", "West", 1, 100.00),
            
            // EDGE CASE: Zero Price Item (Free Gift)
            // Should affect Count, but not Revenue
            new Sale("T5", LocalDate.of(2024, 2, 10), "Promo", "Sticker", "East", 10, 0.00)
        );
        analyzer = new SalesAnalyzer(testSales);
    }

    @Test
    void testCalculateTotalSales() {
        // 1000 + 100 + 100 + 100 + 0 = 1300
        assertEquals(1300.00, analyzer.calculateTotalSales(), 0.01);
    }

    @Test
    void testEmptyDataset() {
        SalesAnalyzer emptyAnalyzer = new SalesAnalyzer(List.of());
        assertEquals(0.0, emptyAnalyzer.calculateTotalSales());
        assertTrue(emptyAnalyzer.getTopProductByQuantity().isEmpty());
        assertTrue(emptyAnalyzer.getSalesByCategory().isEmpty());
    }

    @Test
    void testDataIntegrity_RegionalSumEqualsTotal() {
        // The sum of sales from all regions MUST equal the total sales
        double regionalSum = analyzer.getSalesByCategory().values().stream().mapToDouble(Double::doubleValue).sum();
        assertEquals(analyzer.calculateTotalSales(), regionalSum, 0.01);
    }

    @Test
    void testGetSalesByCategory() {
        Map<String, Double> revenue = analyzer.getSalesByCategory();
        assertEquals(1100.00, revenue.get("Electronics"), 0.01);
        assertEquals(200.00, revenue.get("Clothing"), 0.01);
    }

    @Test
    void testGetTopProductByQuantity() {
        Optional<Map.Entry<String, Integer>> top = analyzer.getTopProductByQuantity();
        assertTrue(top.isPresent());
        // "Sticker" has 10 units (even though price is 0), "Mouse" has 5.
        assertEquals("Sticker", top.get().getKey());
        assertEquals(10, top.get().getValue());
    }

    @Test
    void testGetOrderCountByRegion() {
        Map<String, Long> counts = analyzer.getOrderCountByRegion();
        assertEquals(2, counts.get("North"));
        assertEquals(1, counts.get("South"));
        assertEquals(1, counts.get("East")); // T5
    }

    @Test
    void testGetAverageSalesByCategory() {
        Map<String, Double> avgPrices = analyzer.getAverageSalesByCategory();
        // Electronics: (1000 + 20) / 2 = 510.0
        assertEquals(510.00, avgPrices.get("Electronics"), 0.01);
    }

    @Test
    void testGetHighestValueOrder() {
        Optional<Sale> expensive = analyzer.getHighestValueOrder();
        assertTrue(expensive.isPresent());
        assertEquals("T1", expensive.get().transactionId());
    }

    @Test
    void testGetSalesStatisticsByCategory() {
        Map<String, DoubleSummaryStatistics> stats = analyzer.getSalesStatisticsByCategory();
        
        DoubleSummaryStatistics elecStats = stats.get("Electronics");
        assertEquals(2, elecStats.getCount());
        assertEquals(1100.00, elecStats.getSum(), 0.01);
        assertEquals(100.00, elecStats.getMin(), 0.01); // 5*20
        assertEquals(1000.00, elecStats.getMax(), 0.01); // 1*1000
    }

    @Test
    void testGetMonthlySalesTrend() {
        Map<String, Double> trend = analyzer.getMonthlySalesTrend();
        // Jan: T1(1000) + T2(100) = 1100
        // Feb: T3(100) + T4(100) = 200
        assertEquals(1100.00, trend.get("2024-01"), 0.01);
        assertEquals(200.00, trend.get("2024-02"), 0.01);
    }

    @Test
    void testPartitionOrdersByValue() {
        // Threshold 500. T1(1000) is High. 
        // Low: T2(100), T3(100), T4(100), T5(0).
        Map<Boolean, List<Sale>> partitions = analyzer.partitionOrdersByValue(500.00);
        assertEquals(1, partitions.get(true).size()); // High Value
        assertEquals(4, partitions.get(false).size()); // Low Value
    }
}
