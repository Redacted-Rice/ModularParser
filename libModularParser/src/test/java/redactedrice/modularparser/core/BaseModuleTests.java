package redactedrice.modularparser.core;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.LogSupporter.LogLevel;

class BaseModuleTests {
    final String OBJ_NAME = "Test name";

    @Test
    void constructorSetterTest() {
        BaseModuleTestObj testee = new BaseModuleTestObj(OBJ_NAME);
        assertEquals(testee.getName(), OBJ_NAME);
        assertNull(testee.getModularParser());

        ModularParser parser = mock(ModularParser.class);
        testee.setParser(parser);
        assertEquals(testee.getModularParser(), parser);
    }

    @Test
    void checkModulesCompatibilityTest() {
        BaseModule testee = new BaseModuleTestObj(OBJ_NAME);
        assertTrue(testee.checkModulesCompatibility());
    }

    @Test
    void basicLogMessageTest() {
        final String TEST_LOG = "Test log";
        final String EXPECTED_LOG = OBJ_NAME + ": " + TEST_LOG;

        BaseModule testee = new BaseModuleTestObj(OBJ_NAME);
        ModularParser parser = mock(ModularParser.class);
        testee.setParser(parser);

        assertDoesNotThrow(() -> {
            testee.log(LogLevel.INFO, TEST_LOG);
        });

        LogSupporter logger = mock(LogSupporter.class);
        when(parser.getLogger()).thenReturn(logger);
        when(logger.format(anyString(), anyString(), anyString())).thenReturn(EXPECTED_LOG);

        testee.setParser(parser);

        testee.log(LogLevel.INFO, TEST_LOG);

        verify(logger).format(eq("%s: %s"), eq(OBJ_NAME), eq(TEST_LOG));
        verify(logger).log(eq(LogLevel.INFO), eq(EXPECTED_LOG));
    }

    @Test
    void basicLogFormatTest() {
        final String TEST_LOG = "Test %d log";
        final int INT_VAL = 2;
        final String TEST_FORMATTED_LOG = "Test " + INT_VAL + " log";
        final String EXPECTED_LOG = OBJ_NAME + ": " + TEST_FORMATTED_LOG;

        BaseModule testee = new BaseModuleTestObj(OBJ_NAME);
        ModularParser parser = mock(ModularParser.class);
        testee.setParser(parser);

        assertDoesNotThrow(() -> {
            testee.log(LogLevel.INFO, TEST_LOG, INT_VAL);
        });

        LogSupporter logger = mock(LogSupporter.class);
        when(parser.getLogger()).thenReturn(logger);
        when(logger.format(anyString(), anyInt())).thenReturn(TEST_FORMATTED_LOG);
        when(logger.format(anyString(), anyString(), anyString())).thenReturn(EXPECTED_LOG);

        testee.log(LogLevel.INFO, TEST_LOG, INT_VAL);

        verify(logger).format(eq(TEST_LOG), eq(INT_VAL));
        verify(logger).format(eq("%s: %s"), eq(OBJ_NAME), eq(TEST_FORMATTED_LOG));
        verify(logger).log(eq(LogLevel.INFO), eq(EXPECTED_LOG));
    }

    @Test
    void basicLogErrorTest() {
        final String TEST_LOG = "Test log";
        final String TEST_STACK_TRACE = "line 2: Some Trace";
        final String LOG_WITH_TRACE = TEST_LOG + ". Trace:\n" + TEST_STACK_TRACE;
        final String EXPECTED_LOG = OBJ_NAME + ": " + TEST_LOG;

        BaseModule testee = new BaseModuleTestObj(OBJ_NAME);
        ModularParser parser = mock(ModularParser.class);
        testee.setParser(parser);
        Throwable error = mock(Throwable.class);

        assertDoesNotThrow(() -> {
            testee.log(LogLevel.INFO, error, EXPECTED_LOG);
        });

        LogSupporter logger = mock(LogSupporter.class);
        when(parser.getLogger()).thenReturn(logger);
        when(logger.appendStackTrace(anyString(), any())).thenReturn(LOG_WITH_TRACE);
        when(logger.format(anyString(), anyString(), anyString())).thenReturn(EXPECTED_LOG);

        testee.log(LogLevel.INFO, error, TEST_LOG);

        verify(logger).appendStackTrace(eq(TEST_LOG), eq(error));
        verify(logger).format(eq("%s: %s"), eq(OBJ_NAME), eq(LOG_WITH_TRACE));
        verify(logger).log(eq(LogLevel.INFO), eq(EXPECTED_LOG));
    }
}
