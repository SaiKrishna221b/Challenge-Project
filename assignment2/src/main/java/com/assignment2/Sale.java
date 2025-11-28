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
}

