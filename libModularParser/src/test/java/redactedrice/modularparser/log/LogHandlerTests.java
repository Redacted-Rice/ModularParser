package redactedrice.modularparser.log;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.LogSupporter.LogLevel;

class LogHandlerTests {

    @Test
    void defaultFormat() {
        assertEquals("[INFO ] First", LogHandler.defaultFormat(LogLevel.INFO, "First"));
        assertEquals("[DEBUG] Second", LogHandler.defaultFormat(LogLevel.DEBUG, "Second"));
        assertEquals("[ERROR] Third", LogHandler.defaultFormat(LogLevel.ERROR, "Third"));
    }
}
