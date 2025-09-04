package redactedrice.modularparser.comment;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DefaultSingleLineCommentLineModifierTests {

    static final String NAME = "SingleLineCommentLineModifierTest";
    static final String TOKEN = "//";

    @Test
    void constructorTest() {
        DefaultSingleLineCommentLineModifier testee = new DefaultSingleLineCommentLineModifier(NAME,
                TOKEN);
        assertEquals(NAME, testee.getName());
        assertEquals(TOKEN, testee.token);
    }

    @Test
    void lineContinuersValidTest() {
        DefaultSingleLineCommentLineModifier testee = new DefaultSingleLineCommentLineModifier(NAME,
                TOKEN);
        assertTrue(testee.lineContinuersValid("any string", true));
    }

    @Test
    void lineHasOpenModifierTest() {
        DefaultSingleLineCommentLineModifier testee = new DefaultSingleLineCommentLineModifier(NAME,
                TOKEN);
        assertFalse(testee.lineHasOpenModifier("any string"));
    }

    @Test
    void modifyLine() {
        DefaultSingleLineCommentLineModifier testee = new DefaultSingleLineCommentLineModifier(NAME,
                TOKEN);

        String line = "var x = 5;";
        assertEquals(line, testee.modifyLine(line));
        assertEquals(line, testee.modifyLine("var x = 5;//with a comment"));
        line = "/contri/v/ed line/";
        assertEquals(line, testee.modifyLine(line));
        assertEquals(line, testee.modifyLine("/contri/v/ed line/ //with a comment"));

        assertTrue(testee.modifyLine("// a comment line").isEmpty());
        assertTrue(testee.modifyLine("  //a comment line with no space\t").isEmpty());
        assertTrue(testee.modifyLine("///a comment li//ne w/ith extra //").isEmpty());
    }
}
