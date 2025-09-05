package redactedrice.modularparser.log;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.LogSupporter.LogLevel;

class DefaultCacheLogHandlerTests {

    private DefaultCacheLogHandler testee;

    @BeforeEach
    void setUp() {
        testee = new DefaultCacheLogHandler();
    }

    @Test
    void constructor() {
        assertEquals(DefaultCacheLogHandler.class.getSimpleName(), testee.getName());
        assertTrue(testee.enabled(LogLevel.INFO));
        assertTrue(testee.enabled(LogLevel.ERROR));
    }

    @Test
    void addOrdered() {
        testee.add(LogLevel.INFO, "First");
        testee.add(LogLevel.DEBUG, "Second");
        testee.add(LogLevel.ERROR, "Third");

        List<String> logs = testee.getLogs();
        assertEquals(3, logs.size());
        assertEquals("[INFO ] First", logs.get(0));
        assertEquals("[DEBUG] Second", logs.get(1));
        assertEquals("[ERROR] Third", logs.get(2));
    }

    @Test
    void addOnlyWhenEnabled() {
        testee.enable(LogLevel.DEBUG, false);
        testee.add(LogLevel.DEBUG, "Debug message");

        assertTrue(testee.getLogs().isEmpty());
    }

    @Test
    void enable_shouldToggleLevel() {
        testee.enable(LogLevel.INFO, false);
        assertFalse(testee.enabled(LogLevel.INFO));

        testee.enable(LogLevel.INFO, true);
        assertTrue(testee.enabled(LogLevel.INFO));
    }

    @Test
    void clear() {
        testee.add(LogLevel.INFO, "First");
        testee.add(LogLevel.DEBUG, "Second");
        testee.clear();

        assertTrue(testee.getLogs().isEmpty());

        testee.add(LogLevel.ERROR, "Third");
        assertEquals(1, testee.getLogs().size());

    }
}