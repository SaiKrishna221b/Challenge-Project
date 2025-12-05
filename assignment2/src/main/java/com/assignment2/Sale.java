package com.assignment2;

import java.time.LocalDate;

/**
 * Represents a single sales transaction.
 * 
 * CONCEPT: Java Records (introduced in Java 14/16)
 * The 'record' keyword is a modern alternative to a standard 'class' for data carriers.
 * It is concise and robust because:
 * 1. It is implicitly 'final' and immutable (fields cannot be changed once set).
 * 2. It automatically generates boilerplate code: constructor, getters, equals(), hashCode(), and toString().
 * 3. It fits the Functional Programming paradigm by ensuring data purity (no side effects).
 *
 * @param transactionId Unique identifier for the transaction
 * @param date          Date of the transaction (LocalDate is preferred over legacy Date for type safety)
 * @param category      Product category (e.g., Electronics, Clothing)
 * @param product       Name of the product
 * @param region        Region where the sale occurred
 * @param quantity      Quantity sold
 * @param unitPrice     Price per unit
 */
public record Sale(
    String transactionId,
    LocalDate date,
    String category,
    String product,
    String region,
    int quantity,
    double unitPrice
) {
    
    /**
     * Calculates the total amount for this sale.
     * 
     * DESIGN NOTE: Derived Properties
     * We don't store 'totalAmount' as a field in the CSV or the record.
     * Instead, we calculate it on-the-fly. This ensures data consistency
     * (e.g., total can never get out of sync with quantity * price).
     * 
     * @return The calculated total revenue for this transaction.
     */
    public double getTotalAmount() {
        return quantity * unitPrice;
    }

    /**
     * Helper to extract the Year from the transaction date.
     * 
     * @return The year as an integer (e.g., 2024)
     */
    public int getYear() {
        return date.getYear();
    }

    /**
     * Helper to format the date as "YYYY-MM".
     * Used specifically for grouping sales by month in the analysis.
     * 
     * @return String representation of Year-Month
     */
    public String getMonthYear() {
        // String.format is a clean way to ensure two digits for months (e.g., "2024-01" not "2024-1")
        return String.format("%d-%02d", date.getYear(), date.getMonthValue());
    }
}
