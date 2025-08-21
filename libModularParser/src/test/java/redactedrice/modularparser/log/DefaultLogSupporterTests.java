package redactedrice.modularparser.log;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.LogSupporter.LogLevel;
import redactedrice.modularparser.core.ModularParser;
import redactedrice.modularparser.core.Module;

public class DefaultLogSupporterTests {

    private static String MOD1_NAME = "LogHandler1";
    private static String MOD2_NAME = "LogHandler2";

    private ModularParser parser;
    private DefaultLogSupporter testee;
    LogHandler mod1;
    LogHandler mod2;

    @BeforeEach
    void setup() {
        testee = new DefaultLogSupporter();
        parser = mock(ModularParser.class);
        testee.setParser(parser);
        mod1 = mock(LogHandler.class);
        mod2 = mock(LogHandler.class);

        when(mod1.getName()).thenReturn(MOD1_NAME);
        when(mod2.getName()).thenReturn(MOD2_NAME);
    }

    @Test
    void handleModuleTest() {
        Module modOther = mock(Module.class);

        testee.handleModule(mod1);
        testee.handleModule(mod2);
        testee.handleModule(modOther);

        assertEquals(2, testee.handlers.size());
        assertTrue(testee.handlers.contains(mod1));
        assertTrue(testee.handlers.contains(mod2));
    }

    @Test
    void logTest() {
        testee.handleModule(mod1);
        testee.handleModule(mod2);

        final String LOG_MESSAGE = "This is a test log";
        testee.log(LogLevel.INFO, LOG_MESSAGE);
        verify(mod1).add(LogLevel.INFO, LOG_MESSAGE);
        verify(mod2).add(LogLevel.INFO, LOG_MESSAGE);
    }

    @Test
    void appendStackTraceTest() {
        final String LOG_MESSAGE = "This is a test log";
        final String ERROR_MSG = "Test error";
        final Throwable ERROR = new StackOverflowError(ERROR_MSG);

        String result = testee.appendStackTrace(LOG_MESSAGE, ERROR);

        assertTrue(result.startsWith(LOG_MESSAGE));
        assertTrue(result.contains("Stack Trace:"));
        assertTrue(result.contains("StackOverflowError"));
        assertTrue(result.contains(ERROR_MSG));
        assertTrue(result.contains(
                "at redactedrice.modularparser.log.DefaultLogSupporterTests.appendStackTraceTest"));
    }
}
