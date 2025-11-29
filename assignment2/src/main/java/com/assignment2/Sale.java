package com.assignment2;

import java.time.LocalDate;

/**
 * Represents a single sales transaction.
 * Implemented as a Record for immutability and conciseness.
 *
 * @param transactionId Unique identifier for the transaction
 * @param date Date of the transaction
 * @param category Product category (e.g., Electronics, Clothing)
 * @param product Name of the product
 * @param region Region where the sale occurred
 * @param quantity Quantity sold
 * @param unitPrice Price per unit
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
     * @return quantity * unitPrice
     */
    public double getTotalAmount() {
        return quantity * unitPrice;
    }

    /**
     * Helper to extract the Year from the transaction date.
     * @return The year as an integer (e.g., 2024)
     */
    public int getYear() {
        return date.getYear();
    }

    /**
     * Helper to format the date as "YYYY-MM".
     * Useful for grouping monthly trends.
     * @return String representation of Year-Month
     */
    public String getMonthYear() {
        return String.format("%d-%02d", date.getYear(), date.getMonthValue());
    }
}
