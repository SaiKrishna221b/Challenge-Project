package com.assignment2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Main entry point logic.
 * 
 * Why verify Main?
 * We need to ensure that command-line arguments (or lack thereof) correctly 
 * trigger the appropriate execution modes (Standard vs. Chunking) without crashing.
 */
class MainTest {

    // Streams to capture Console Output/Error for assertion
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUp() {
        // Hijack the system output streams before each test
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void restoreStreams() {
        // Restore them so we can see test results in the console
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void testMainWithChunkSizeArgument() {
        /*
         * Scenario: Run with "5000" as an argument.
         * Expected: The app detects the arg and launches Chunk Mode automatically.
         */
        Main.main(new String[]{"5000"});

        String output = outContent.toString();

        // Verify the mode selection logic
        assertTrue(output.contains("--- Starting Chunked Processing (Size: 5000) ---"), 
            "Should have started chunk processing with size 5000");
            
        // Verify it didn't try to ask for user input
        assertFalse(output.contains("Select Execution Mode"), 
            "Interactive menu should not appear in CLI argument mode");
            
        // Verify it completed
        assertTrue(output.contains("Status: SUCCESS"));
    }

    @Test
    void testMainWithInvalidArgument() {
        /*
         * Scenario: Run with "banana" as an argument.
         * Expected: The app catches the NumberFormatException, prints an error,
         * and safely falls back to Standard Mode.
         */
        Main.main(new String[]{"banana"});

        String errOutput = errContent.toString();
        String stdOutput = outContent.toString();

        // Verify Error Handling
        assertTrue(errOutput.contains("Invalid argument provided (not a number)"), 
            "Should print error message to stderr");
        
        // Verify Fallback Behavior
        assertTrue(stdOutput.contains("--- Loading Sales Data ---"), 
            "Should fall back to Standard Analysis");
    }
}
