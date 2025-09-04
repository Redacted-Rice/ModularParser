package redactedrice.modularparser.lineformer;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.ModularParser;

class DefaultGroupingLineModifierTests {

    static final String NAME = "GroupingLineModifierTest";
    static final String START_TOKEN = "(";
    static final String END_TOKEN = ")";

    @Test
    void constructorTest() {
        boolean removeTokens = true;
        DefaultGroupingLineModifier testee = new DefaultGroupingLineModifier(NAME, START_TOKEN,
                END_TOKEN, removeTokens);

        assertEquals(START_TOKEN, testee.startToken);
        assertEquals(END_TOKEN, testee.endToken);
        assertEquals(removeTokens, testee.removeTokens);
    }

    @Test
    void isValidTest() {
        boolean removeTokens = true;
        DefaultGroupingLineModifier testee = new DefaultGroupingLineModifier(NAME, START_TOKEN,
                END_TOKEN, removeTokens);
        ModularParser parser = mock(ModularParser.class);
        testee.setParser(parser);

        boolean isComplete = true;
        assertTrue(testee.lineContinuersValid("1 (2) 3", isComplete));
        assertTrue(testee.lineContinuersValid("()(1 (2) )3", isComplete));
        assertFalse(testee.lineContinuersValid("1 (2 (3)", isComplete));
        assertFalse(testee.lineContinuersValid("1 )(2) (3", isComplete));

        isComplete = false;
        assertTrue(testee.lineContinuersValid("1 (2) 3", isComplete));
        assertTrue(testee.lineContinuersValid("()(1 (2) )3", isComplete));
        assertTrue(testee.lineContinuersValid("1 (2 (3)", isComplete));
        assertFalse(testee.lineContinuersValid("1 )(2) (3", isComplete));
    }

    @Test
    void lineHasOpenModifierTest() {
        boolean removeTokens = true;
        DefaultGroupingLineModifier testee = new DefaultGroupingLineModifier(NAME, START_TOKEN,
                END_TOKEN, removeTokens);

        assertTrue(testee.lineHasOpenModifier("(1(2)"));
        assertTrue(testee.lineHasOpenModifier("(1"));
        assertTrue(testee.lineHasOpenModifier("(1)(1(2)1)0(1(2)1"));

        assertFalse(testee.lineHasOpenModifier("1"));
        assertFalse(testee.lineHasOpenModifier("(1)"));
        assertFalse(testee.lineHasOpenModifier("(1(2(3)2(3)))"));

        assertFalse(testee.lineHasOpenModifier(")"));
        assertFalse(testee.lineHasOpenModifier("())))(("));

        // This does not handle validity just if the count
        // is right
        assertFalse(testee.lineHasOpenModifier("))(("));
    }

    @Test
    void modifyLineTest() {
        boolean removeTokens = true;
        DefaultGroupingLineModifier testee = new DefaultGroupingLineModifier(NAME, START_TOKEN,
                END_TOKEN, removeTokens);

        // Test with no tokens
        assertEquals("0 1 2 3", testee.modifyLine("0 1 2 3"));

        // Test with removing tokens
        assertEquals("0 1 2 0", testee.modifyLine("0(1\n2)0"));
        assertEquals("0 1 2 3 2 1 0", testee.modifyLine("0(1(2(3)2)1)0"));
        assertEquals("0 1 2 0", testee.modifyLine("0   ( 1\n\t 2)\t\t0"));

        // Test without removing tokens
        removeTokens = false;
        testee = new DefaultGroupingLineModifier(NAME, START_TOKEN, END_TOKEN, removeTokens);
        assertEquals("0 (1 2) 0", testee.modifyLine("0(1\n2)0"));
        assertEquals("0 (1 (2 (3) 2) 1) 0", testee.modifyLine("0(1(2(3)2)1)0"));
        assertEquals("0 (1 2) 0", testee.modifyLine("0   ( 1\n\t 2)\t\t0"));
    }
}
