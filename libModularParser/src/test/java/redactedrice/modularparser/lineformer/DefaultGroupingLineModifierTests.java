package redactedrice.modularparser.lineformer;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.ModularParser;
import redactedrice.modularparser.core.Response;

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
        assertTrue(testee.lineContinuersValid("1 (2( 3) )(4) 5", isComplete));
        assertTrue(testee.lineContinuersValid("0 ((1 (2) )3)", isComplete));
        assertFalse(testee.lineContinuersValid("1 (2 (3)", isComplete));
        assertFalse(testee.lineContinuersValid("1 )(2) (3", isComplete));

        isComplete = false;
        assertTrue(testee.lineContinuersValid("1 (2) 3", isComplete));
        assertTrue(testee.lineContinuersValid("()(1 (2) )3", isComplete));
        assertTrue(testee.lineContinuersValid("1 (2( 3) )(4) 5", isComplete));
        assertTrue(testee.lineContinuersValid("0 ((1 (2) )3)", isComplete));
        assertTrue(testee.lineContinuersValid("1 (2 (3)", isComplete));
        assertFalse(testee.lineContinuersValid("1 )(2) (3", isComplete));
    }

    @Test
    void lineHasOpenModifierHasOpenGroupTest() {
        boolean removeTokens = true;
        DefaultGroupingLineModifier testee = new DefaultGroupingLineModifier(NAME, START_TOKEN,
                END_TOKEN, removeTokens);

        assertTrue(testee.lineHasOpenModifier("(1(2)"));
        assertTrue(testee.lineHasOpenModifier("(1"));
        assertTrue(testee.lineHasOpenModifier("(1)(1(2)1)0(1(2)1"));
        assertTrue(testee.lineHasOpenModifier("somestuff (1)(1(2)1)0(1(2)1 other stuff"));
        assertTrue(testee.hasOpenGroup("(1(2)"));
        assertTrue(testee.hasOpenGroup("(1"));
        assertTrue(testee.hasOpenGroup("(1)(1(2)1)0(1(2)1"));
        assertTrue(testee.hasOpenGroup("somestuff (1)(1(2)1)0(1(2)1 other stuff"));

        assertFalse(testee.lineHasOpenModifier("1"));
        assertFalse(testee.lineHasOpenModifier("(1)"));
        assertFalse(testee.lineHasOpenModifier("(1(2(3)2(3)))"));
        assertFalse(testee.lineHasOpenModifier("some stuff (1(2(3)2(3)))otherstuff"));
        assertFalse(testee.hasOpenGroup("1"));
        assertFalse(testee.hasOpenGroup("(1)"));
        assertFalse(testee.hasOpenGroup("(1(2(3)2(3)))"));
        assertFalse(testee.hasOpenGroup("some stuff (1(2(3)2(3)))otherstuff"));

        assertFalse(testee.lineHasOpenModifier(")"));
        assertFalse(testee.lineHasOpenModifier("())))(("));
        assertFalse(testee.hasOpenGroup(")"));
        assertFalse(testee.hasOpenGroup("())))(("));

        // This does not handle validity just if the count
        // is right
        assertFalse(testee.lineHasOpenModifier("))(("));
        assertFalse(testee.hasOpenGroup("))(("));
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

    @Test
    void tryGetNextGroupTest() {
        boolean removeTokens = true;
        DefaultGroupingLineModifier testee = new DefaultGroupingLineModifier(NAME, START_TOKEN,
                END_TOKEN, removeTokens);
        ModularParser parser = mock(ModularParser.class);
        testee.setParser(parser);

        assertTrue(testee.tryGetNextGroup("bad )( order", true).wasNotHandled());
        assertTrue(testee.tryGetNextGroup("an ( open line", true).wasNotHandled());
        assertTrue(testee.tryGetNextGroup("no groupers", true).wasNotHandled());

        Response<String[]> res = testee.tryGetNextGroup("pretext (1 2 3) post text", true);
        assertTrue(res.wasHandled());
        assertEquals("pretext", res.getValue()[0]);
        assertEquals("1 2 3", res.getValue()[1]);
        assertEquals("post text", res.getValue()[2]);

        res = testee.tryGetNextGroup("pretext (1 2 3) post text", false);
        assertTrue(res.wasHandled());
        assertEquals("pretext", res.getValue()[0]);
        assertEquals("(1 2 3)", res.getValue()[1]);
        assertEquals("post text", res.getValue()[2]);
    }

    @Test
    void isEmptyGroupTest() {
        boolean removeTokens = true;
        DefaultGroupingLineModifier testee = new DefaultGroupingLineModifier(NAME, START_TOKEN,
                END_TOKEN, removeTokens);

        assertTrue(testee.isEmptyGroup("()"));
        assertTrue(testee.isEmptyGroup("\t  () \n"));
        assertTrue(testee.isEmptyGroup("(  \t\n)"));

        assertFalse(testee.isEmptyGroup("(something)"));
        assertFalse(testee.isEmptyGroup("uhoh()"));
        assertFalse(testee.isEmptyGroup("()uhoh"));
    }

    @Test
    void tryGetGroupHelperTest() {
        boolean removeTokens = true;
        DefaultGroupingLineModifier testee = new DefaultGroupingLineModifier(NAME, "<<", ">>",
                removeTokens);
        ModularParser parser = mock(ModularParser.class);
        testee.setParser(parser);

        assertTrue(testee.tryGetGroupHelper("bad >> << order", true, true).wasError());

        Response<String[]> res = testee.tryGetGroupHelper("pretext <<1 2 3 >> post text", true,
                true);
        assertTrue(res.wasHandled());
        assertEquals("pretext", res.getValue()[0]);
        assertEquals("1 2 3", res.getValue()[1]);
        assertEquals("post text", res.getValue()[2]);

        res = testee.tryGetGroupHelper("pretext <<1 2 3 >> post text <<with>> group", true, false);
        assertTrue(res.wasHandled());
        assertEquals("pretext", res.getValue()[0]);
        assertEquals("<<1 2 3 >>", res.getValue()[1]);
        assertEquals("post text <<with>> group", res.getValue()[2]);

        res = testee.tryGetGroupHelper("pretext <<group with subgroup <<1 2 3>>>> post text", true,
                true);
        assertTrue(res.wasHandled());
        assertEquals("pretext", res.getValue()[0]);
        assertEquals("group with subgroup <<1 2 3>>", res.getValue()[1]);
        assertEquals("post text", res.getValue()[2]);

        res = testee.tryGetGroupHelper("no parens", true, false);
        assertTrue(res.wasHandled());
        assertEquals("no parens", res.getValue()[0]);
        assertNull(res.getValue()[1]);
        assertNull(res.getValue()[2]);

        assertTrue(testee.tryGetGroupHelper("not << closed ", true, false).wasError());

        res = testee.tryGetGroupHelper("not << closed ", false, false);
        assertTrue(res.wasHandled());
        assertEquals("not", res.getValue()[0]);
        assertEquals("<< closed ", res.getValue()[1]);
        assertNull(res.getValue()[2]);
    }
}
