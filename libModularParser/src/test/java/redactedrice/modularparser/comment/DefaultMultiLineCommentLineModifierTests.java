package redactedrice.modularparser.comment;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

class DefaultMultiLineCommentLineModifierTests {

    static final String NAME = "SingleLineCommentLineModifierTest";
    static final String START_TOKEN = "/*";
    static final String END_TOKEN = "*/";

    @Test
    void constructorTest() {
        DefaultMutliLineCommentLineModifier testee = new DefaultMutliLineCommentLineModifier(NAME,
                START_TOKEN, END_TOKEN);
        assertEquals(NAME, testee.getName());
        assertEquals(START_TOKEN, testee.startToken);
        assertEquals(END_TOKEN, testee.endToken);
    }

    @Test
    void recursivelyRemoveTokenPairsTest() {
        // also tests removeTokenPair
        DefaultMutliLineCommentLineModifier testee = new DefaultMutliLineCommentLineModifier(NAME,
                START_TOKEN, END_TOKEN);

        // complete line tests
        String line = "var x = 5;";
        assertEquals(line, testee.recursivelyRemoveTokenPairs("var x = /*inline*/ 5;", false));
        assertEquals(line, testee.recursivelyRemoveTokenPairs("var x = /*inline*/ 5;", true));
        assertEquals(line, testee.recursivelyRemoveTokenPairs(
                "var x = /*inline\n but with \n multiple lines*/ 5;", false));
        assertEquals(line, testee.recursivelyRemoveTokenPairs(
                "var x = /*inline\n but with \n multiple lines*/ 5;", true));
        assertEquals(line, testee.recursivelyRemoveTokenPairs(
                "var /*comment here*/ x = /*inline*/ 5/*nospace*/;", false));
        assertEquals(line, testee.recursivelyRemoveTokenPairs(
                "var /*comment here*/ x = /*inline*/ 5/*nospace*/;", true));
        assertEquals("", testee.recursivelyRemoveTokenPairs("/**/", false));
        assertEquals("", testee.recursivelyRemoveTokenPairs("/**/", true));
        assertEquals("",
                testee.recursivelyRemoveTokenPairs("/*\na longer\nblock\n of comment*/", false));
        assertEquals("",
                testee.recursivelyRemoveTokenPairs("/*\na longer\nblock\n of comment*/", true));
        assertEquals("", testee.recursivelyRemoveTokenPairs("/* Two /* starts is fine */", false));
        assertEquals("", testee.recursivelyRemoveTokenPairs("/* Two /* starts is fine */", true));

        line = " /* open comment";
        assertNull(testee.recursivelyRemoveTokenPairs(line, false));
        assertEquals(line, testee.recursivelyRemoveTokenPairs(line, true));
        assertNull(testee.recursivelyRemoveTokenPairs(
                "/* closed comment */ /*another*/ /* open comment", false));
        assertEquals(line, testee.recursivelyRemoveTokenPairs(
                "/* closed comment */ /*another*/ /* open comment", true));

        assertNull(testee.recursivelyRemoveTokenPairs("/* two */ closed */", false));
        assertNull(testee.recursivelyRemoveTokenPairs("/* two */ closed */", true));
        assertNull(testee.recursivelyRemoveTokenPairs("closed */ only", false));
        assertNull(testee.recursivelyRemoveTokenPairs("closed */ only", true));
        assertNull(testee.recursivelyRemoveTokenPairs("Startes */ with /* */ closed ", false));
        assertNull(testee.recursivelyRemoveTokenPairs("Startes */ with /* */ closed ", true));
    }

    @Test
    void lineContinuersValidTest() {
        DefaultMutliLineCommentLineModifier testee = new DefaultMutliLineCommentLineModifier(NAME,
                START_TOKEN, END_TOKEN);

        // Stick to basic tests since we already tested recursivelyRemoveTokenPairs
        assertTrue(testee.lineContinuersValid("var /* comment */ x = 5;", true));
        assertTrue(testee.lineContinuersValid("var /* comment */ x = 5;", false));

        assertFalse(testee.lineContinuersValid("var x = 5; /* open", true));
        assertTrue(testee.lineContinuersValid("var x = 5; /* open", false));
    }

    @Test
    void lineHasOpenModifierTest() {
        DefaultMutliLineCommentLineModifier testee = new DefaultMutliLineCommentLineModifier(NAME,
                START_TOKEN, END_TOKEN);

        assertFalse(testee.lineHasOpenModifier("var x = 5;"));
        assertFalse(testee.lineHasOpenModifier("/* complete\n\ncomment */"));
        assertFalse(
                testee.lineHasOpenModifier("var /* inline */ x = /* another inline comment */ 5;"));
        assertFalse(testee.lineHasOpenModifier("closed */ only"));
        assertFalse(testee.lineHasOpenModifier("/* two */ closed */"));

        assertTrue(testee.lineHasOpenModifier("/* open comment"));
        assertTrue(testee.lineHasOpenModifier("var x = 5;/*"));
        assertTrue(testee.lineHasOpenModifier("var /*inline*/x = 5;/*"));
        assertTrue(testee.lineHasOpenModifier("var /*inline\n*/x = 5;/*\nmultline open"));
    }

    @Test
    void modifyLine() {
        DefaultMutliLineCommentLineModifier testee = spy(
                new DefaultMutliLineCommentLineModifier(NAME, START_TOKEN, END_TOKEN));

        // Stick to basic tests since we already tested recursivelyRemoveTokenPairs
        assertEquals("var x = 5;", testee.modifyLine("var /* comment */ x = 5/*comment 2*/;"));

        doNothing().when(testee).log(any(), anyString(), any());
        String line = "var x = 5; /* open";
        assertEquals(line, testee.modifyLine(line));
        verify(testee).log(any(), anyString(), any());
    }
}
