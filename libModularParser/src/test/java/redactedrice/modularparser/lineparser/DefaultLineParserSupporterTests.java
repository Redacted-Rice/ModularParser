package redactedrice.modularparser.lineparser;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.ModularParser;

class DefaultLineParserSupporterTests {

    static final String LINE_1 = "Line 1";
    static final String LINE_2 = "Line 2";
    static final String LINE_3 = "Line 3";

    private ModularParser parser;
    private DefaultLineParserSupporter testee;

    @BeforeEach
    void setup() {
        testee = new DefaultLineParserSupporter();
        parser = mock(ModularParser.class);
        testee.setParser(parser);
    }

    @Test
    void constructorTest() {
        assertEquals(DefaultLineParserSupporter.class.getSimpleName(), testee.getName());
    }

    @Test
    void parseLineTest() {
        final String TEST_LINE = "TEST LINE";

        LineParser parser1 = mock(LineParser.class);
        LineParser parser2 = mock(LineParser.class);
        testee.handleModule(parser1);
        testee.handleModule(parser2);

        when(parser1.tryParseLine(any())).thenReturn(true);
        when(parser2.tryParseLine(any())).thenReturn(true);
        assertTrue(testee.parseLine(TEST_LINE));
        verify(parser2, never()).tryParseLine(any());

        when(parser1.tryParseLine(any())).thenReturn(false);
        assertTrue(testee.parseLine(TEST_LINE));
        verify(parser1, times(2)).tryParseLine(any());
        verify(parser2).tryParseLine(any());

        when(parser2.tryParseLine(any())).thenReturn(false);
        assertFalse(testee.parseLine(TEST_LINE));
        verify(parser1, times(3)).tryParseLine(any());
        verify(parser2, times(2)).tryParseLine(any());
    }
}
