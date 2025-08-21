package redactedrice.modularparser.core;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.LogSupporter.LogLevel;

class BaseModuleTests {
    final String OBJ_NAME = "Test name";

    private class BaseModuleTester extends BaseModule {
        public BaseModuleTester(String name) {
            super(name);
        }
    }

    @Test
    void constructorSetterTest() {
        BaseModuleTester testee = new BaseModuleTester(OBJ_NAME);
        assertEquals(OBJ_NAME, testee.getName());
        assertNull(testee.parser);

        ModularParser parser = mock(ModularParser.class);
        testee.setParser(parser);
        assertEquals(parser, testee.parser);

        testee.setModuleRefs();
    }

    @Test
    void checkModulesCompatibilityTest() {
        BaseModule testee = new BaseModuleTester(OBJ_NAME);
        assertTrue(testee.checkModulesCompatibility());
    }

    @Test
    void basicLogMessageTest() {
        final String TEST_LOG = "Test log";
        final String EXPECTED_LOG = OBJ_NAME + ": " + TEST_LOG;

        ModularParser parser = mock(ModularParser.class);
        BaseModule testee = new BaseModuleTester(OBJ_NAME);
        testee.setParser(parser);

        assertDoesNotThrow(() -> {
            testee.log(LogLevel.INFO, TEST_LOG);
        });

        LogSupporter logger = mock(LogSupporter.class);
        when(parser.getLogger()).thenReturn(logger);

        testee.log(LogLevel.INFO, TEST_LOG);
        verify(logger).log(eq(LogLevel.INFO), eq(EXPECTED_LOG));
    }

    @Test
    void logFormatTest() {
        final String TEST_LOG = "Test %d log";
        final int INT_VAL = 2;
        final String TEST_FORMATTED_LOG = "Test " + INT_VAL + " log";
        final String EXPECTED_LOG = OBJ_NAME + ": " + TEST_FORMATTED_LOG;

        ModularParser parser = mock(ModularParser.class);
        BaseModule testee = new BaseModuleTester(OBJ_NAME);
        testee.setParser(parser);

        assertDoesNotThrow(() -> {
            testee.log(LogLevel.INFO, TEST_LOG, INT_VAL);
        });

        LogSupporter logger = mock(LogSupporter.class);
        when(parser.getLogger()).thenReturn(logger);

        testee.log(LogLevel.INFO, TEST_LOG, INT_VAL);
        verify(logger).log(eq(LogLevel.INFO), eq(EXPECTED_LOG));
    }

    @Test
    void logErrorTest() {
        final String TEST_LOG = "Test log";
        final String TEST_STACK_TRACE = "line 2: Some Trace";
        final String LOG_WITH_TRACE = TEST_LOG + ". Trace:\n" + TEST_STACK_TRACE;
        final String EXPECTED_LOG = OBJ_NAME + ": " + LOG_WITH_TRACE;

        ModularParser parser = mock(ModularParser.class);
        BaseModule testee = new BaseModuleTester(OBJ_NAME);
        testee.setParser(parser);
        Throwable error = mock(Throwable.class);

        assertDoesNotThrow(() -> {
            testee.log(LogLevel.INFO, false, error, "Log before logger set");
        });

        LogSupporter logger = mock(LogSupporter.class);
        when(parser.getLogger()).thenReturn(logger);
        when(logger.appendStackTrace(anyString(), any())).thenReturn(LOG_WITH_TRACE);

        testee.log(LogLevel.INFO, error, TEST_LOG);

        verify(logger).appendStackTrace(eq(TEST_LOG), eq(error));
        verify(logger).log(eq(LogLevel.INFO), eq(EXPECTED_LOG));
    }

    @Test
    void logNotifyTest() {
        final String TEST_LOG = "Test log";

        ModularParser parser = mock(ModularParser.class);
        BaseModule testee = new BaseModuleTester(OBJ_NAME);
        testee.setParser(parser);

        LogSupporter logger = mock(LogSupporter.class);
        when(parser.getLogger()).thenReturn(logger);

        testee.log(LogLevel.ERROR, false, TEST_LOG);
        verify(parser, never()).notifyError();

        testee.log(LogLevel.ERROR, TEST_LOG);
        verify(parser).notifyError();

        testee.log(LogLevel.ABORT, TEST_LOG);
        verify(parser).notifyAbort();
    }
}
