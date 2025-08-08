package redactedrice.modularparser.lineformer;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.LineFormerSupporter.LineRange;
import redactedrice.modularparser.core.ModularParser;

public class DefaultLineFormerSupporterTests {

    final String LINE_1 = "Line 1";
    final String LINE_2 = "Line 2";
    final String LINE_3 = "Line 3";

    private BufferedReader reader;
    private ModularParser parser;
    private DefaultLineFormerSupporter testee;

    @BeforeEach
    void setup() {
        testee = new DefaultLineFormerSupporter();
        reader = mock(BufferedReader.class);
        parser = mock(ModularParser.class);
        testee.setParser(parser);
    }

    @Test
    void setResetReaderTest() throws IOException {
        testee.setReader(reader);
        assertEquals(testee.reader, reader);
        assertEquals(testee.lineNumberStart, 0);
        assertEquals(testee.lineNumberEnd, 0);

        testee.lineNumberStart = 5;
        testee.lineNumberEnd = 8;
        testee.resetReader();
        assertEquals(testee.lineNumberStart, 0);
        assertEquals(testee.lineNumberEnd, 0);
        verify(reader).reset();
        verify(parser).resetStatus();

        doAnswer(o -> {
            throw new IOException();
        }).when(reader).reset();
        assertDoesNotThrow(() -> {
            testee.resetReader();
        });
    }

    @Test
    void getNextLineTest() throws IOException {
        assertNull(testee.getNextLine());

        testee.setReader(reader);

        when(reader.readLine()).thenReturn(LINE_1, LINE_2);
        assertEquals(testee.getNextLine(), LINE_1);
        assertEquals(testee.getNextLine(), LINE_2);

        when(reader.readLine()).thenAnswer(o -> {
            throw new IOException();
        });
        assertNull(testee.getNextLine());
    }

    @Test
    void getNextLogicalLineNoModifierTest() throws IOException {
        testee.setReader(reader);

        when(reader.readLine()).thenReturn(null);
        assertNull(testee.getNextLogicalLine());
        assertEquals(1, testee.lineNumberStart);
        assertEquals(1, testee.lineNumberEnd);

        when(reader.readLine()).thenReturn(LINE_1, LINE_2, null);
        testee.lineNumberStart = 0;
        testee.lineNumberEnd = 0;
        assertEquals(LINE_1, testee.getNextLogicalLine());
        assertEquals(1, testee.lineNumberStart);
        assertEquals(1, testee.lineNumberEnd);

        assertEquals(LINE_2, testee.getNextLogicalLine());
        assertEquals(2, testee.lineNumberStart);
        assertEquals(2, testee.lineNumberEnd);

        assertNull(testee.getNextLogicalLine());
        assertEquals(3, testee.lineNumberStart);
        assertEquals(3, testee.lineNumberEnd);
    }

    @Test
    void getNextLogicalLineSimpleModifierTest() throws IOException {
        final String LINE_1_MODIFIED_1 = LINE_1 + " MODIFIED";
        final String LINE_1_MODIFIED_2 = LINE_1_MODIFIED_1 + " Again";
        testee.setReader(reader);

        LineModifier modifier1 = mock(LineModifier.class);
        LineModifier modifier2 = mock(LineModifier.class);
        testee.handleModule(modifier1);
        testee.handleModule(modifier2);

        when(reader.readLine()).thenReturn(LINE_1);
        when(modifier1.lineContinuersValid(any(), anyBoolean())).thenReturn(true);
        when(modifier2.lineContinuersValid(any(), anyBoolean())).thenReturn(true);
        when(modifier1.lineHasOpenModifier(any())).thenReturn(false);
        when(modifier2.lineHasOpenModifier(any())).thenReturn(false);
        when(modifier1.modifyLine(LINE_1)).thenReturn(LINE_1_MODIFIED_1);
        when(modifier2.modifyLine(LINE_1_MODIFIED_1)).thenReturn(LINE_1_MODIFIED_2);

        // Happy case with two modifications
        assertEquals(LINE_1_MODIFIED_2, testee.getNextLogicalLine());
        assertEquals(1, testee.lineNumberStart);
        assertEquals(1, testee.lineNumberEnd);

        // Test a line that becomes empty
        when(modifier1.modifyLine(LINE_1)).thenReturn("", LINE_1_MODIFIED_1);
        assertEquals(LINE_1_MODIFIED_2, testee.getNextLogicalLine());
        assertEquals(3, testee.lineNumberStart);
        assertEquals(3, testee.lineNumberEnd);
    }

    @Test
    void getNextLogicalLineContinueModifierTest() throws IOException {
        final String COMBINED_1_2 = LINE_1 + LINE_2;
        final String COMBINED_ALL = COMBINED_1_2 + LINE_3;
        testee.setReader(reader);

        LineModifier modifier1 = mock(LineModifier.class);
        LineModifier modifier2 = mock(LineModifier.class);
        testee.handleModule(modifier1);
        testee.handleModule(modifier2);

        // We will read 3 lines. All 3 will be from the second modifier
        // but we should expect it to reset and pass it to the first one
        // as well once it finishes its modifier
        when(reader.readLine()).thenReturn(LINE_1, LINE_2, LINE_3, null);
        when(modifier1.lineContinuersValid(any(), anyBoolean())).thenReturn(true);
        when(modifier2.lineContinuersValid(any(), anyBoolean())).thenReturn(true);
        when(modifier1.lineHasOpenModifier(any())).thenReturn(false);
        when(modifier2.lineHasOpenModifier(any())).thenReturn(true, true, false);

        when(modifier1.modifyLine(LINE_1)).thenReturn(LINE_1);
        when(modifier1.modifyLine(COMBINED_ALL)).thenReturn(COMBINED_ALL);
        when(modifier2.modifyLine(COMBINED_ALL)).thenReturn(COMBINED_ALL);

        assertEquals(COMBINED_ALL, testee.getNextLogicalLine());
        verify(modifier1).lineHasOpenModifier(LINE_1);
        // Ensure it reset the loop and called the original modifier
        verify(modifier1).lineHasOpenModifier(COMBINED_ALL);
        assertEquals(1, testee.lineNumberStart);
        assertEquals(3, testee.lineNumberEnd);

        // Now test an unended extension
        when(reader.readLine()).thenReturn(LINE_1, LINE_2, null);
        when(modifier2.lineHasOpenModifier(any())).thenReturn(true);
        when(modifier1.modifyLine(any())).thenReturn(LINE_1);
        when(modifier2.modifyLine(any())).thenReturn(LINE_1);
        assertNull(testee.getNextLogicalLine());
    }

    @Test
    void getNextLogicalLineInvalidLineTest() throws IOException {
        testee.setReader(reader);

        LineModifier modifier1 = mock(LineModifier.class);
        LineModifier modifier2 = mock(LineModifier.class);
        testee.handleModule(modifier1);
        testee.handleModule(modifier2);

        when(reader.readLine()).thenReturn(LINE_1);
        when(modifier1.lineContinuersValid(any(), anyBoolean())).thenReturn(false);

        // Happy case with two modifications
        assertNull(testee.getNextLogicalLine());
    }

    @Test
    void getCurrentLineRangeTest() {
        testee.lineNumberStart = 0;
        testee.lineNumberEnd = 0;
        LineRange res = testee.getCurrentLineRange();
        assertEquals(res.start(), 0);
        assertEquals(res.end(), 0);

        testee.lineNumberStart = 3;
        testee.lineNumberEnd = 5;
        res = testee.getCurrentLineRange();
        assertEquals(res.start(), 3);
        assertEquals(res.end(), 5);
    }
}
