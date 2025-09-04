package redactedrice.modularparser.core;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.LogSupporter.LogLevel;

class ModularParserStatusTests {
    static final String LOGGER_NAME = "TestLogger";
    static final String LOG_FORMAT = "This is %s!";
    static final String LOG_DATA = "a test";
    static final String LOG_FULL = "This is a test!";

    private LogSupporter mockLogger;
    private ModularParser testee;

    @BeforeEach
    void setup() {
        testee = new ModularParser();
        mockLogger = mock(LogSupporter.class);
        when(mockLogger.getName()).thenReturn(LOGGER_NAME);
    }

    @Test
    void logOrStdErrTest() {
        // Test without logger
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            testee.logOrStdErr(LOG_FORMAT, LOG_DATA);
        } finally {
            System.setOut(originalOut);
        }

        String printed = outContent.toString().trim();
        assertEquals(LOG_FULL, printed);

        // Now test with logger
        testee.addModule(mockLogger);
        testee.logOrStdErr(LOG_FORMAT, LOG_DATA);
        verify(mockLogger).log(LogLevel.ERROR, LOG_FULL);
    }

    @Test
    void notifyErrorTest() {
        // Without logger
        assertEquals(ModularParser.Status.OK, testee.getStatus());
        testee.notifyError();
        assertEquals(ModularParser.Status.ERROR, testee.getStatus());

        // Now test with logger
        final String ERROR_LOG = "ModularParser: First Error Signaled";
        testee.addModule(mockLogger);
        testee.status = ModularParser.Status.OK;
        testee.notifyError();
        verify(mockLogger).log(LogLevel.ERROR, ERROR_LOG);
        assertEquals(ModularParser.Status.ERROR, testee.getStatus());

        // Calling again should not log a second time
        testee.notifyError();
        verify(mockLogger, times(1)).log(LogLevel.ERROR, ERROR_LOG);
        assertEquals(ModularParser.Status.ERROR, testee.getStatus());
    }

    @Test
    void notifyAbortTest() {
        // Without logger
        assertEquals(ModularParser.Status.OK, testee.getStatus());
        testee.notifyAbort();
        assertEquals(ModularParser.Status.ABORT, testee.getStatus());

        // Now test with logger
        final String ABORT_LOG = "ModularParser: First Abort Signaled";
        testee.addModule(mockLogger);
        testee.status = ModularParser.Status.OK;
        testee.notifyAbort();
        verify(mockLogger).log(LogLevel.ERROR, ABORT_LOG);
        assertEquals(ModularParser.Status.ABORT, testee.getStatus());

        // Calling again should not log a second time
        testee.notifyAbort();
        verify(mockLogger, times(1)).log(LogLevel.ERROR, ABORT_LOG);
        assertEquals(ModularParser.Status.ABORT, testee.getStatus());
    }

    @Test
    void resetStatusTest() {
        testee.status = ModularParser.Status.ABORT;
        testee.resetStatus();
        assertEquals(ModularParser.Status.OK, testee.getStatus());

        testee.status = ModularParser.Status.ERROR;
        testee.resetStatus();
        assertEquals(ModularParser.Status.OK, testee.getStatus());
    }

    @Test
    void abortedTest() {
        testee.status = ModularParser.Status.OK;
        assertFalse(testee.aborted());
        testee.status = ModularParser.Status.ERROR;
        assertFalse(testee.aborted());

        testee.status = ModularParser.Status.ABORT;
        assertTrue(testee.aborted());
    }
}
