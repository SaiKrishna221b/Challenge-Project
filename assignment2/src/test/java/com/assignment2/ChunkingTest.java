package com.assignment2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the Advanced Chunking Engine.
 * 
 * Purpose:
 * Validate that processing data in small "chunks" (batches) yields the 
 * correct results, proving the system is scalable for Big Data scenarios.
 */
class ChunkingTest {

    // Capture System.out to verify console output
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    // JUnit 5 TempDir: Automatically creates and cleans up a temp folder
    @TempDir
    Path tempDir;

    private Path testCsvPath;

    @BeforeEach
    void setUp() throws IOException {
        // Redirect System.out to capture what the application prints
        System.setOut(new PrintStream(outContent));

        // Create a sample CSV file for controlled testing
        testCsvPath = tempDir.resolve("test_sales_chunking.csv");
        List<String> lines = List.of(
            "TransactionId,Date,Category,Product,Region,Quantity,UnitPrice",
            "T1,2024-01-01,Electronics,Laptop,North,1,1000.00",
            "T2,2024-01-02,Electronics,Mouse,South,5,20.00",
            "T3,2024-02-01,Clothing,T-Shirt,North,2,50.00",
            "T4,2024-02-05,Clothing,Jeans,West,1,100.00"
        );
        Files.write(testCsvPath, lines);
    }

    @AfterEach
    void restoreStreams() {
        // Restore stdout so we can see normal logs again
        System.setOut(originalOut);
    }

    @Test
    void testProcessInChunks_SmallBatchSize() throws IOException {
        /*
         * Scenario: Process the main 20,000 record file in batches of 5000.
         * 
         * Expected:
         * 1. 4 full chunks processed (20000 / 5000 = 4).
         * 2. Final stats match the known truth (Total: $110M+).
         */
        SalesAnalyzer.processInChunks("sales_data.csv", 5000);
        
        String output = outContent.toString();
        
        // Verify Process Flow
        assertTrue(output.contains("--- Starting Chunked Processing"));
        assertTrue(output.contains("Processed 4 chunks"));
        assertTrue(output.contains("Status: SUCCESS"));
        
        // Verify Data Accuracy
        assertTrue(output.contains("$110407964.25"), "Total Revenue incorrect");
        assertTrue(output.contains("Hat (4638 units)"), "Top Product incorrect");
    }
    
    @Test
    void testProcessInChunks_TinyBatch() throws IOException {
        /*
         * Scenario: Boundary Test with weird batch size (1999).
         * 
         * Why: This forces a "remainder" chunk at the end.
         * 20000 / 1999 = 10 full chunks + 1 partial chunk of 10 items.
         * Total chunks = 11.
         */
        SalesAnalyzer.processInChunks("sales_data.csv", 1999);
        
        String output = outContent.toString();
        
        assertTrue(output.contains("Status: SUCCESS"));
        assertTrue(output.contains("Processed 11 chunks"), "Expected 11 chunks (10 full + 1 partial)"); 
        assertTrue(output.contains("$110407964.25"), "Total Revenue incorrect");
    }
}
