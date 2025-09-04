package redactedrice.modularparser.log;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.EnumSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.LogSupporter.LogLevel;

class DefaultConsoleLogHandlerTests {
    @Test
    void constructorTest() {
        DefaultConsoleLogHandler testee = new DefaultConsoleLogHandler();
        assertEquals(DefaultConsoleLogHandler.class.getSimpleName(), testee.getName());
        assertTrue(testee.enabledLevels.contains(LogLevel.DEBUG));
        assertTrue(testee.enabledLevels.contains(LogLevel.INFO));
        assertTrue(testee.enabledLevels.contains(LogLevel.WARN));
        assertTrue(testee.enabledLevels.contains(LogLevel.ERROR));
        assertTrue(testee.enabledLevels.contains(LogLevel.ABORT));

        Set<LogLevel> levels = EnumSet.noneOf(LogLevel.class);
        levels.clear();
        levels.add(LogLevel.DEBUG);
        levels.add(LogLevel.INFO);
        testee = new DefaultConsoleLogHandler(levels);
        assertTrue(testee.enabledLevels.contains(LogLevel.DEBUG));
        assertTrue(testee.enabledLevels.contains(LogLevel.INFO));
        assertFalse(testee.enabledLevels.contains(LogLevel.WARN));
        assertFalse(testee.enabledLevels.contains(LogLevel.ERROR));
        assertFalse(testee.enabledLevels.contains(LogLevel.ABORT));
    }

    @Test
    void addFormatEnabledTest() {
        Set<LogLevel> levels = EnumSet.noneOf(LogLevel.class);
        levels.clear();
        levels.add(LogLevel.WARN);
        levels.add(LogLevel.ERROR);
        levels.add(LogLevel.ABORT);
        DefaultConsoleLogHandler testee = new DefaultConsoleLogHandler(levels);

        // Backup original streams
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;

        // Create capture streams
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();

        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));

        final String TEST_LOG = "Debug log";
        testee.add(LogLevel.WARN, TEST_LOG);
        testee.add(LogLevel.ERROR, TEST_LOG);
        testee.add(LogLevel.ABORT, TEST_LOG);

        // Verify output
        String outResult = outContent.toString();
        String errResult = errContent.toString();

        // Assertions (if using JUnit)
        assertTrue(outResult.contains("[WARN ] " + TEST_LOG));
        assertTrue(errResult.contains("[ERROR] " + TEST_LOG));
        assertTrue(errResult.contains("[ABORT] " + TEST_LOG));

        // Restore original streams
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    void enableTest() {
        DefaultConsoleLogHandler testee = new DefaultConsoleLogHandler();
        assertTrue(testee.enabledLevels.contains(LogLevel.INFO));
        assertTrue(testee.enabledLevels.contains(LogLevel.WARN));

        testee.enable(LogLevel.INFO, true);
        assertTrue(testee.enabledLevels.contains(LogLevel.INFO));
        assertTrue(testee.enabledLevels.contains(LogLevel.WARN));

        testee.enable(LogLevel.INFO, false);
        assertFalse(testee.enabledLevels.contains(LogLevel.INFO));
        assertTrue(testee.enabledLevels.contains(LogLevel.WARN));

        testee.enable(LogLevel.INFO, true);
        assertTrue(testee.enabledLevels.contains(LogLevel.INFO));
        assertTrue(testee.enabledLevels.contains(LogLevel.WARN));
    }
}
