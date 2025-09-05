package redactedrice.modularparser.lineformer;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.LineFormerSupporter.LineRange;
import redactedrice.modularparser.core.ModularParser;
import redactedrice.modularparser.core.Response;

class DefaultLineFormerSupporterTests {

    static final String LINE_1 = "Line 1";
    static final String LINE_2 = "Line 2";
    static final String LINE_3 = "Line 3";

    private BufferedReader reader;
    private ModularParser parser;
    private DefaultLineFormerSupporter testee;

    @BeforeEach
    void setup() {
        testee = spy(new DefaultLineFormerSupporter());
        reader = mock(BufferedReader.class);
        parser = mock(ModularParser.class);
        testee.setParser(parser);
    }

    @Test
    void constructorTest() {
        assertEquals(DefaultLineFormerSupporter.class.getSimpleName(), testee.getName());
        assertEquals(0, testee.lineNumberStart);
        assertEquals(0, testee.lineNumberEnd);
        assertNull(testee.reader);
    }

    @Test
    void setResetReaderTest() throws IOException {
        assertDoesNotThrow(() -> {
            testee.resetReader();
        });

        testee.setReader(reader);
        assertEquals(testee.reader, reader);
        assertEquals(0, testee.lineNumberStart);
        assertEquals(0, testee.lineNumberEnd);

        testee.lineNumberStart = 5;
        testee.lineNumberEnd = 8;
        testee.resetReader();
        assertEquals(0, testee.lineNumberStart);
        assertEquals(0, testee.lineNumberEnd);
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
        assertEquals(LINE_1, testee.getNextLine());
        assertEquals(LINE_2, testee.getNextLine());

        when(reader.readLine()).thenAnswer(o -> {
            throw new IOException();
        });
        assertNull(testee.getNextLine());
    }

    @Test
    void getUntilNextLine() {
        assertNull(testee.getUntilNextLine());

        doReturn("", "   ", LINE_1).when(testee).getNextLine();
        assertEquals(LINE_1, testee.getUntilNextLine());

        doReturn("", "   ", null, LINE_1).when(testee).getNextLine();
        assertNull(testee.getUntilNextLine());
    }

    @Test
    void getNextLogicalLineTest() {
        LineModifier modifier1 = mock(LineModifier.class);
        LineModifier modifier2 = mock(LineModifier.class);
        testee.handleModule(modifier1);
        testee.handleModule(modifier2);

        doReturn(null).when(testee).getUntilNextLine();
        assertNull(testee.getNextLogicalLine());

        doReturn(LINE_1).when(testee).getUntilNextLine();
        doReturn(Response.error("test")).when(testee).gatherLine(any(), any());
        assertNull(testee.getNextLogicalLine());

        doReturn(Response.notHandled()).when(testee).gatherLine(any(), any());
        when(modifier1.modifyLine(LINE_1)).thenReturn(LINE_1);
        when(modifier2.modifyLine(LINE_1)).thenReturn(LINE_1);
        assertEquals(LINE_1, testee.getNextLogicalLine());

        doReturn(LINE_1, LINE_2).when(testee).getUntilNextLine();
        when(modifier1.modifyLine(LINE_1)).thenReturn(" ");
        when(modifier2.modifyLine(" ")).thenReturn(" ");
        when(modifier1.modifyLine(LINE_2)).thenReturn(LINE_2);
        when(modifier2.modifyLine(LINE_2)).thenReturn(LINE_2);
        assertEquals(LINE_2, testee.getNextLogicalLine());

        doReturn(Response.is(LINE_2), Response.notHandled()).when(testee).gatherLine(eq(modifier1),
                any());
        doReturn(Response.notHandled()).when(testee).gatherLine(eq(modifier2), any());
        assertEquals(LINE_2, testee.getNextLogicalLine());
    }

    @Test
    void gatherLineTest() {
        LineModifier modifier = mock(LineModifier.class);
        when(modifier.lineContinuersValid(any(), anyBoolean())).thenReturn(true);
        when(modifier.lineHasOpenModifier(any())).thenReturn(false);
        assertTrue(testee.gatherLine(modifier, LINE_1).wasNotHandled());

        when(modifier.lineContinuersValid(any(), anyBoolean())).thenReturn(true);
        when(modifier.lineHasOpenModifier(any())).thenReturn(true);
        doReturn(null).when(testee).getNextLine();
        assertTrue(testee.gatherLine(modifier, LINE_1).wasError());

        when(modifier.lineHasOpenModifier(any())).thenReturn(true, false);
        doReturn(LINE_2).when(testee).getNextLine();
        Response<String> response = testee.gatherLine(modifier, LINE_1);
        assertTrue(response.wasValueReturned());
        assertEquals(LINE_1 + LINE_2, response.getValue());

        when(modifier.lineContinuersValid(any(), anyBoolean())).thenReturn(false);
        assertTrue(testee.gatherLine(modifier, LINE_1).wasError());
    }

    @Test
    void getCurrentLineRangeTest() {
        testee.lineNumberStart = 0;
        testee.lineNumberEnd = 0;
        LineRange res = testee.getCurrentLineRange();
        assertEquals(0, res.start());
        assertEquals(0, res.end());

        testee.lineNumberStart = 3;
        testee.lineNumberEnd = 5;
        res = testee.getCurrentLineRange();
        assertEquals(3, res.start());
        assertEquals(5, res.end());
    }
}
