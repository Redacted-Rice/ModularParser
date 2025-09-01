package redactedrice.modularparser.lineparser;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.ModularParser;

public class DefaultLambdaParserTests {

    final String NAME = "LambdaParserTest";
    final String KEYWORD = "print";

    private ModularParser parser;
    private DefaultLambdaParserFn fn;
    private DefaultLambdaParser testee;

    @BeforeEach
    void setup() {
        fn = mock(DefaultLambdaParserFn.class);
        testee = spy(new DefaultLambdaParser(NAME, fn, KEYWORD));
        parser = mock(ModularParser.class);
        testee.setParser(parser);
    }

    @Test
    void constructorTest() {
        assertEquals(fn, testee.handler);
        assertEquals(NAME, testee.getName());
        assertEquals(KEYWORD, testee.getKeyword());
    }

    @Test
    void tryParseLine() {
        doReturn(false).when(testee).matches(any());
        assertFalse(testee.tryParseLine("test line"));
        verify(fn, never()).handle(any());

        doReturn(true).when(testee).matches(any());
        assertTrue(testee.tryParseLine("print 'test'"));
        verify(fn).handle(any());
    }
}
