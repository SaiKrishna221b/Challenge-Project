package com.assignment2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;

class MainTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void testMainWithChunkSizeArgument() {
        // Simulate running "java Main 5000"
        Main.main(new String[]{"5000"});

        String output = outContent.toString();

        // Should trigger Chunking Mode
        assertTrue(output.contains("--- Starting Chunked Processing (Size: 5000) ---"));
        // Should NOT show the menu
        assertFalse(output.contains("Select Execution Mode"));
        // Should finish successfully
        assertTrue(output.contains("Status: SUCCESS"));
    }

    @Test
    void testMainWithInvalidArgument() {
        // Simulate running "java Main banana"
        Main.main(new String[]{"banana"});

        String errOutput = errContent.toString();
        String stdOutput = outContent.toString();

        // Should warn about invalid argument
        assertTrue(errOutput.contains("Invalid argument. Defaulting to Standard Mode."));
        
        // Should fall back to Standard Mode (loading all data)
        assertTrue(stdOutput.contains("--- Loading Sales Data ---"));
    }
}

