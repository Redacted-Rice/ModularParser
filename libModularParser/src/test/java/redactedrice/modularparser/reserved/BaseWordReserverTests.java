package redactedrice.modularparser.reserved;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redactedrice.modularparser.reserved.ReservedWordSupporter.ReservedType;

class BaseWordReserverTests {
    final String NAME = "WordReserver";
    final String RESERVED_1 = "alias";
    final String RESERVED_2 = "var";
    final String RESERVED_3 = "const";
    final String RESERVED_4 = "scope";

    private BaseWordReserver testee;

    private class BaseWordReserverTester extends BaseWordReserver {
        public BaseWordReserverTester(String name) {
            super(name);
        }
    }

    @BeforeEach
    void setup() {
        testee = new BaseWordReserverTester(NAME);
        testee.reservedWords.put(RESERVED_1, ReservedType.EXCLUSIVE);
        testee.reservedWords.put(RESERVED_2, ReservedType.EXCLUSIVE);
        testee.reservedWords.put(RESERVED_3, ReservedType.SHAREABLE);
        testee.reservedWords.put(RESERVED_4, ReservedType.SHAREABLE);
    }

    @Test
    void constructorSetterTest() {
        assertEquals(NAME, testee.getName());
    }

    @Test
    void isReservedWordTest() {
        assertTrue(testee.isReservedWord(RESERVED_1));
        assertTrue(testee.isReservedWord(RESERVED_3));
        assertFalse(testee.isReservedWord("SomeWord"));

        assertTrue(testee.isReservedWord(RESERVED_1, Optional.of(ReservedType.EXCLUSIVE)));
        assertFalse(testee.isReservedWord(RESERVED_1, Optional.of(ReservedType.SHAREABLE)));
        assertTrue(testee.isReservedWord(RESERVED_3, Optional.of(ReservedType.SHAREABLE)));
        assertFalse(testee.isReservedWord(RESERVED_3, Optional.of(ReservedType.EXCLUSIVE)));
    }

    @Test
    void getAllReservedWordsTest() {
        Map<String, ReservedType> result = testee.getAllReservedWords();
        assertEquals(4, result.size());
        assertEquals(ReservedType.EXCLUSIVE, result.get(RESERVED_1));
        assertEquals(ReservedType.EXCLUSIVE, result.get(RESERVED_2));
        assertEquals(ReservedType.SHAREABLE, result.get(RESERVED_3));
        assertEquals(ReservedType.SHAREABLE, result.get(RESERVED_4));
    }

    @Test
    void getReservedWordsTest() {
        Set<String> result = testee.getReservedWords(ReservedType.EXCLUSIVE);
        assertEquals(2, result.size());
        assertTrue(result.contains(RESERVED_1));
        assertTrue(result.contains(RESERVED_2));

        result = testee.getReservedWords(ReservedType.SHAREABLE);
        assertEquals(2, result.size());
        assertTrue(result.contains(RESERVED_3));
        assertTrue(result.contains(RESERVED_4));
    }
}
