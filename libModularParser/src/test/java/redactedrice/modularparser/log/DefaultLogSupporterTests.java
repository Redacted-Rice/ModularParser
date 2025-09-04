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

class DefaultLogSupporterTests {

    static final String MOD1_NAME = "LogHandler1";
    static final String MOD2_NAME = "LogHandler2";

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
    void constructorTest() {
        assertEquals(DefaultLogSupporter.class.getSimpleName(), testee.getName());
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

        final String logMessage = "This is a test log";
        testee.log(LogLevel.INFO, logMessage);
        verify(mod1).add(LogLevel.INFO, logMessage);
        verify(mod2).add(LogLevel.INFO, logMessage);
    }

    @Test
    void appendStackTraceTest() {
        final String logMessage = "This is a test log";
        final String errorMsg = "Test error";
        final Throwable error = new StackOverflowError(errorMsg);

        String result = testee.appendStackTrace(logMessage, error);

        assertTrue(result.startsWith(logMessage));
        assertTrue(result.contains("Stack Trace:"));
        assertTrue(result.contains("StackOverflowError"));
        assertTrue(result.contains(errorMsg));
        assertTrue(result.contains(
                "at redactedrice.modularparser.log.DefaultLogSupporterTests.appendStackTraceTest"));
    }
}
