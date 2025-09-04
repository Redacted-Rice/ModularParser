package redactedrice.modularparser.reserved;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BaseWordReserverTests {
    static final String NAME = "WordReserver";
    static final String KEYWORD = "alias";
    static final String RESERVED_1 = "print";
    static final String RESERVED_2 = "println";
    static final String RESERVED_3 = "echo";

    private BaseKeywordReserver testee;

    private class BaseWordReserverTester extends BaseKeywordReserver {
        public BaseWordReserverTester(String name) {
            super(name, KEYWORD);
        }
    }

    @BeforeEach
    void setup() {
        testee = new BaseWordReserverTester(NAME);
    }

    @Test
    void constructorSetterTest() {
        assertEquals(NAME, testee.getName());
        assertEquals(KEYWORD, testee.keyword);
    }

    @Test
    void isReservedWordTest() {
        assertTrue(testee.isReservedWord(KEYWORD));
        assertFalse(testee.isReservedWord("SomeWord"));
    }

    @Test
    void getReservedWordsTest() {
        Set<String> result = testee.getReservedWords();
        assertEquals(1, result.size());
        assertTrue(result.contains(KEYWORD));
    }
}
