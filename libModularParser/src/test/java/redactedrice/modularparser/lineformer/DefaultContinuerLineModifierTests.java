package redactedrice.modularparser.lineformer;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class DefaultContinuerLineModifierTests {

    final String NAME = "GroupingContinuerModifierTest";
    final String TOKEN = "->";

    @Test
    void constructorTest() {
        boolean removeTokens = true;
        DefaultContinuerLineModifier testee = new DefaultContinuerLineModifier(NAME, TOKEN,
                removeTokens);
        assertEquals(TOKEN, testee.token);
        assertEquals(TOKEN, testee.token);

        removeTokens = false;
        testee = new DefaultContinuerLineModifier(NAME, TOKEN, removeTokens);
        assertEquals(TOKEN, testee.token);
    }

    @Test
    void lineContinuersValidTest() {
        boolean removeTokens = true;
        DefaultContinuerLineModifier testee = new DefaultContinuerLineModifier(NAME, TOKEN,
                removeTokens);
        assertTrue(testee.lineContinuersValid("any string", true));
    }

    @Test
    void hasOpenModifierTest() {
        boolean removeTokens = true;
        DefaultContinuerLineModifier testee = new DefaultContinuerLineModifier(NAME, TOKEN,
                removeTokens);

        assertTrue(testee.lineHasOpenModifier("1 ->"));
        assertTrue(testee.lineHasOpenModifier("1 -> 2 ->"));

        assertFalse(testee.lineHasOpenModifier("1 -> 2"));
        assertFalse(testee.lineHasOpenModifier("1 2"));
    }

    @Test
    void modifyLineTest() {
        boolean removeTokens = true;
        DefaultContinuerLineModifier testee = new DefaultContinuerLineModifier(NAME, TOKEN,
                removeTokens);

        assertEquals("1 2", testee.modifyLine("1 2"));
        assertEquals("1 ", testee.modifyLine("1\t\t ->   "));
        assertEquals("1 2 ", testee.modifyLine("1->\t2 ->"));
        assertEquals("1 2", testee.modifyLine("1 ->\n2"));

        removeTokens = false;
        testee = new DefaultContinuerLineModifier(NAME, TOKEN, removeTokens);
        assertEquals("1 2", testee.modifyLine("1 2"));
        assertEquals("1 -> ", testee.modifyLine("1\t\t ->   "));
        assertEquals("1 -> 2 -> ", testee.modifyLine("1->\t2 ->"));
        assertEquals("1 -> 2", testee.modifyLine("1 ->\n2"));
    }
}
