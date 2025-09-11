package redactedrice.modularparser.core;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.LogSupporter.LogLevel;

class BaseModuleTests {
    static final String OBJ_NAME = "Test name";

    private class BaseModuleTester extends BaseModule {
        public BaseModuleTester(String name) {
            super(name);
        }
    }

    @Test
    void constructor_setters() {
        BaseModuleTester testee = new BaseModuleTester(OBJ_NAME);
        assertEquals(OBJ_NAME, testee.getName());
        assertNull(testee.parser);

        ModularParser parser = mock(ModularParser.class);
        testee.setParser(parser);
        assertEquals(parser, testee.parser);

        testee.setModuleRefs();
    }

    @Test
    void checkModulesCompatibility() {
        BaseModule testee = new BaseModuleTester(OBJ_NAME);
        assertTrue(testee.checkModulesCompatibility());
    }

    @Test
    void log() {
        final String testLog = "Test log";
        final String expectedLog = OBJ_NAME + ": " + testLog;

        ModularParser parser = mock(ModularParser.class);
        BaseModule testee = new BaseModuleTester(OBJ_NAME);
        testee.setParser(parser);

        assertDoesNotThrow(() -> {
            testee.log(LogLevel.INFO, testLog);
        });

        LogSupporter logger = mock(LogSupporter.class);
        when(parser.getLogger()).thenReturn(logger);

        testee.log(LogLevel.INFO, testLog);
        verify(logger).log(LogLevel.INFO, expectedLog);
    }

    @Test
    void log_format() {
        final String testLog = "Test %d log";
        final int INT_VAL = 2;
        final String TEST_FORMATTED_LOG = "Test " + INT_VAL + " log";
        final String expectedLog = OBJ_NAME + ": " + TEST_FORMATTED_LOG;

        ModularParser parser = mock(ModularParser.class);
        BaseModule testee = new BaseModuleTester(OBJ_NAME);
        testee.setParser(parser);

        assertDoesNotThrow(() -> {
            testee.log(LogLevel.INFO, testLog, INT_VAL);
        });

        LogSupporter logger = mock(LogSupporter.class);
        when(parser.getLogger()).thenReturn(logger);

        testee.log(LogLevel.INFO, testLog, INT_VAL);
        verify(logger).log(LogLevel.INFO, expectedLog);
    }

    @Test
    void log_error() {
        final String testLog = "Test log";
        final String testStackTrace = "line 2: Some Trace";
        final String logWithTrace = testLog + ". Trace:\n" + testStackTrace;
        final String expectedLog = OBJ_NAME + ": " + logWithTrace;

        ModularParser parser = mock(ModularParser.class);
        BaseModule testee = new BaseModuleTester(OBJ_NAME);
        testee.setParser(parser);
        Throwable error = mock(Throwable.class);

        assertDoesNotThrow(() -> {
            testee.log(LogLevel.INFO, false, error, "Log before logger set");
        });

        LogSupporter logger = mock(LogSupporter.class);
        when(parser.getLogger()).thenReturn(logger);
        when(logger.appendStackTrace(anyString(), any())).thenReturn(logWithTrace);

        testee.log(LogLevel.INFO, error, testLog);

        verify(logger).appendStackTrace(testLog, error);
        verify(logger).log(LogLevel.INFO, expectedLog);
    }

    @Test
    void log_notifyNullLogger() {
        final String testLog = "Test log";

        ModularParser parser = mock(ModularParser.class);
        BaseModule testee = new BaseModuleTester(OBJ_NAME);
        testee.setParser(parser);

        // Try all variations without a logger. These should still notify of error
        testee.log(LogLevel.ERROR, testLog);
        verify(parser).notifyError();
        testee.log(LogLevel.ERROR, true, testLog);
        verify(parser, times(2)).notifyError();
        testee.log(LogLevel.ERROR, "%s", testLog);
        verify(parser, times(3)).notifyError();
        testee.log(LogLevel.ERROR, true, "%s", testLog);
        verify(parser, times(4)).notifyError();
        testee.log(LogLevel.ERROR, new RuntimeException(), testLog);
        verify(parser, times(5)).notifyError();
        testee.log(LogLevel.ERROR, true, new RuntimeException(), testLog);
        verify(parser, times(6)).notifyError();
        clearInvocations(parser);

        // Now try all variations with notify false
        testee.log(LogLevel.ERROR, false, testLog);
        verify(parser, never()).notifyError();
        testee.log(LogLevel.ERROR, false, "%s", testLog);
        verify(parser, never()).notifyError();
        testee.log(LogLevel.ERROR, false, new RuntimeException(), testLog);
        verify(parser, never()).notifyError();
    }

    @Test
    void log_notifyValidLogger() {
        final String testLog = "Test log";

        ModularParser parser = mock(ModularParser.class);
        BaseModule testee = new BaseModuleTester(OBJ_NAME);
        testee.setParser(parser);
        LogSupporter logger = mock(LogSupporter.class);
        when(parser.getLogger()).thenReturn(logger);

        // Test all variations of log
        testee.log(LogLevel.ERROR, testLog);
        verify(parser).notifyError();
        testee.log(LogLevel.ERROR, true, testLog);
        verify(parser, times(2)).notifyError();
        testee.log(LogLevel.ERROR, "%s", testLog);
        verify(parser, times(3)).notifyError();
        testee.log(LogLevel.ERROR, true, "%s", testLog);
        verify(parser, times(4)).notifyError();
        testee.log(LogLevel.ERROR, new RuntimeException(), testLog);
        verify(parser, times(5)).notifyError();
        testee.log(LogLevel.ERROR, true, new RuntimeException(), testLog);
        verify(parser, times(6)).notifyError();
        clearInvocations(parser);

        // Now try all variations with notify false
        testee.log(LogLevel.ERROR, false, testLog);
        verify(parser, never()).notifyError();
        testee.log(LogLevel.ERROR, false, "%s", testLog);
        verify(parser, never()).notifyError();
        testee.log(LogLevel.ERROR, false, new RuntimeException(), testLog);
        verify(parser, never()).notifyError();

        // Try an abort
        testee.log(LogLevel.ABORT, testLog);
        verify(parser).notifyAbort();
    }
}
