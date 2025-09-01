package redactedrice.modularparser.lineparser;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.ModularParser;

public class DefaultLinStartMatcherTests {

    final String NAME = "LineStartMatcher";
    final String KEYWORD = "print";

    private ModularParser parser;
    private BaseLineStartMatcher testee;

    private class BaseLineStartMatcherTester extends BaseLineStartMatcher {
        protected BaseLineStartMatcherTester(String name, String keyword) {
            super(name, keyword);
        }

        @Override
        public boolean tryParseLine(String logicalLine) {
            // Doesn't matter for this test
            return false;
        }
    }

    @BeforeEach
    void setup() {
        testee = new BaseLineStartMatcherTester(NAME, KEYWORD);
        parser = mock(ModularParser.class);
        testee.setParser(parser);
    }

    @Test
    void constructorTest() {
        assertEquals(NAME, testee.getName());
        assertEquals(KEYWORD, testee.getKeyword());
        assertTrue(testee.getReservedWords().contains(KEYWORD));
    }

    @Test
    void matches() {
        assertFalse(testee.matches("test line"));
        assertFalse(testee.matches("not at start print"));
        assertFalse(testee.matches("sprint yeah"));
        assertFalse(testee.matches("printing something"));

        assertTrue(testee.matches("print 'test'"));
        assertTrue(testee.matches("print"));
        assertTrue(testee.matches("print print printprint"));
    }
}
