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
 * Verifies that batch processing produces the same results as standard processing.
 */
class ChunkingTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @TempDir
    Path tempDir;

    private Path testCsvPath;

    @BeforeEach
    void setUp() throws IOException {
        // Redirect System.out to capture output
        System.setOut(new PrintStream(outContent));

        // Create a temporary CSV file with known data
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
        System.setOut(originalOut);
    }

    @Test
    void testProcessInChunks_SmallBatchSize() throws IOException {
        // We cannot test "processInChunks" directly easily because it reads from ClassPath resources
        // and we just created a file on the filesystem.
        // However, checking the code, "processInChunks" takes a filename string and looks in Resources.
        // To make this testable without modifying the main code to accept File objects, 
        // we can't easily inject the temp file unless we put it in target/classes.
        
        // STRATEGY CHANGE: 
        // Since we can't modify classpath at runtime easily, we will rely on the fact that
        // SalesAnalyzer.fromCsv works, and we are testing the LOGIC parity.
        // But `processInChunks` is static and void.
        
        // To really test this properly, we would need to refactor `processInChunks` to accept an InputStream.
        // Since we want to minimize code changes, we'll skip the file-based test here 
        // and instead verify that the main logic components exist.
        
        // If you want to run this test, we must ensure "sales_data.csv" exists in resources, 
        // which it does (the big 20k file).
        // We can run the chunk processor on the REAL file with a weird chunk size.
        
        SalesAnalyzer.processInChunks("sales_data.csv", 5000);
        
        String output = outContent.toString();
        
        // Verify Output Format
        assertTrue(output.contains("--- Starting Chunked Processing"));
        assertTrue(output.contains("Processed 4 chunks")); // 20000 / 5000 = 4
        assertTrue(output.contains("Status: SUCCESS"));
        
        // Verify Key Metrics (we know the total from previous runs: $110,407,964.25)
        assertTrue(output.contains("$110407964.25"));
        assertTrue(output.contains("Hat (4638 units)"));
    }
    
    @Test
    void testProcessInChunks_TinyBatch() throws IOException {
        // Stress test with tiny batches to ensure boundary conditions work
        // 20000 records, batch size 1999 (creates a remainder)
        SalesAnalyzer.processInChunks("sales_data.csv", 1999);
        
        String output = outContent.toString();
        
        assertTrue(output.contains("Status: SUCCESS"));
        // 20000 / 1999 = 10 chunks + 1 remainder = 11 chunks total
        assertTrue(output.contains("Processed 11 chunks")); 
        assertTrue(output.contains("$110407964.25"));
    }
}

