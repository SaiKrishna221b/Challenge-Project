package com.assignment2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SalesAnalyzerTest {

    private SalesAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        // Create a small, controlled dataset for testing
        List<Sale> testSales = Arrays.asList(
            new Sale("T1", LocalDate.of(2024, 1, 1), "Electronics", "Laptop", "North", 1, 1000.00),
            new Sale("T2", LocalDate.of(2024, 1, 2), "Electronics", "Mouse", "South", 5, 20.00),
            new Sale("T3", LocalDate.of(2024, 1, 3), "Clothing", "T-Shirt", "North", 2, 50.00),
            new Sale("T4", LocalDate.of(2024, 1, 4), "Clothing", "Jeans", "West", 1, 100.00)
        );
        analyzer = new SalesAnalyzer(testSales);
    }

    @Test
    void testCalculateTotalRevenue() {
        // 1*1000 + 5*20 + 2*50 + 1*100 = 1000 + 100 + 100 + 100 = 1300
        assertEquals(1300.00, analyzer.calculateTotalRevenue(), 0.01);
    }

    @Test
    void testGetRevenueByCategory() {
        Map<String, Double> revenue = analyzer.getRevenueByCategory();
        
        // Electronics: 1000 + 100 = 1100
        assertEquals(1100.00, revenue.get("Electronics"), 0.01);
        // Clothing: 100 + 100 = 200
        assertEquals(200.00, revenue.get("Clothing"), 0.01);
    }

    @Test
    void testGetTopSellingProduct() {
        Optional<Map.Entry<String, Integer>> top = analyzer.getTopSellingProduct();
        
        assertTrue(top.isPresent());
        assertEquals("Mouse", top.get().getKey()); // 5 units
        assertEquals(5, top.get().getValue());
    }

    @Test
    void testGetSalesCountByRegion() {
        Map<String, Long> counts = analyzer.getSalesCountByRegion();
        
        assertEquals(2, counts.get("North")); // T1, T3
        assertEquals(1, counts.get("South")); // T2
        assertEquals(1, counts.get("West"));  // T4
    }

    @Test
    void testGetAveragePriceByCategory() {
        Map<String, Double> avgPrices = analyzer.getAveragePriceByCategory();
        
        // Electronics: (1000 + 20) / 2 = 510.0
        assertEquals(510.00, avgPrices.get("Electronics"), 0.01);
        // Clothing: (50 + 100) / 2 = 75.0
        assertEquals(75.00, avgPrices.get("Clothing"), 0.01);
    }

    @Test
    void testGetMostExpensiveSale() {
        Optional<Sale> expensive = analyzer.getMostExpensiveSale();
        
        assertTrue(expensive.isPresent());
        assertEquals("T1", expensive.get().transactionId());
        assertEquals(1000.00, expensive.get().getTotalAmount(), 0.01);
    }
    
    @Test
    void testEmptyDataset() {
        SalesAnalyzer emptyAnalyzer = new SalesAnalyzer(List.of());
        assertEquals(0.0, emptyAnalyzer.calculateTotalRevenue());
        assertTrue(emptyAnalyzer.getTopSellingProduct().isEmpty());
    }
}

