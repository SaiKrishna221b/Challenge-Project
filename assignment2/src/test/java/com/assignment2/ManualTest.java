package com.assignment2;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A manual test script to verify SalesAnalyzer logic without external JUnit dependencies.
 */
public class ManualTest {

    public static void main(String[] args) {
        System.out.println("--- Running Manual Diagnostic Tests ---");
        
        int passed = 0;
        int failed = 0;

        // Setup Test Data
        List<Sale> testSales = Arrays.asList(
            new Sale("T1", LocalDate.of(2024, 1, 1), "Electronics", "Laptop", "North", 1, 1000.00),
            new Sale("T2", LocalDate.of(2024, 1, 2), "Electronics", "Mouse", "South", 5, 20.00),
            new Sale("T3", LocalDate.of(2024, 1, 3), "Clothing", "T-Shirt", "North", 2, 50.00),
            new Sale("T4", LocalDate.of(2024, 1, 4), "Clothing", "Jeans", "West", 1, 100.00)
        );
        SalesAnalyzer analyzer = new SalesAnalyzer(testSales);

        // Test 1: Total Sales
        try {
            double expected = 1300.00;
            double actual = analyzer.calculateTotalSales();
            if (Math.abs(expected - actual) < 0.01) {
                System.out.println("[PASS] Calculate Total Sales");
                passed++;
            } else {
                System.err.println("[FAIL] Calculate Total Sales. Expected: " + expected + ", Got: " + actual);
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Calculate Total Sales threw exception: " + e.getMessage());
            failed++;
        }

        // Test 2: Sales by Category
        try {
            Map<String, Double> revenue = analyzer.getSalesByCategory();
            if (Math.abs(revenue.get("Electronics") - 1100.00) < 0.01 && 
                Math.abs(revenue.get("Clothing") - 200.00) < 0.01) {
                System.out.println("[PASS] Sales By Category");
                passed++;
            } else {
                System.err.println("[FAIL] Sales By Category. Expected {Electronics=1100, Clothing=200}, Got: " + revenue);
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Sales By Category threw exception: " + e.getMessage());
            failed++;
        }

        // Test 3: Top Product
        try {
            Optional<Map.Entry<String, Integer>> top = analyzer.getTopProductByQuantity();
            if (top.isPresent() && top.get().getKey().equals("Mouse") && top.get().getValue() == 5) {
                System.out.println("[PASS] Top Product By Quantity");
                passed++;
            } else {
                System.err.println("[FAIL] Top Product. Expected Mouse (5), Got: " + (top.isPresent() ? top.get().getKey() : "None"));
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Top Product threw exception: " + e.getMessage());
            failed++;
        }

        // Test 4: Order Count by Region
        try {
            Map<String, Long> counts = analyzer.getOrderCountByRegion();
            if (counts.get("North") == 2 && counts.get("South") == 1 && counts.get("West") == 1) {
                System.out.println("[PASS] Order Count By Region");
                passed++;
            } else {
                System.err.println("[FAIL] Order Count By Region. Expected {North=2, ...}, Got: " + counts);
                failed++;
            }
        } catch (Exception e) {
            System.err.println("[FAIL] Order Count By Region threw exception: " + e.getMessage());
            failed++;
        }

        System.out.println("---------------------------------------");
        System.out.println("Tests Completed. Passed: " + passed + ", Failed: " + failed);
        
        if (failed > 0) {
            System.exit(1);
        }
    }
}
