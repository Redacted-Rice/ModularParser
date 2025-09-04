package redactedrice.modularparser.reserved;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.ModularParser;

class DefaultReservedWordSupporterTests {

    static final String MOD_1_NAME = "WordReserver1";
    static final String MOD_2_NAME = "WordReserver2";

    static final String RESERVED_1 = "alias";
    static final String RESERVED_2 = "var";
    static final String RESERVED_3 = "const";
    static final String RESERVED_4 = "scope";

    private ModularParser parser;
    private DefaultReservedWordSupporter testee;
    WordReserver mod1;
    WordReserver mod2;

    @BeforeEach
    void setup() {
        testee = new DefaultReservedWordSupporter();
        parser = mock(ModularParser.class);
        testee.setParser(parser);

        mod1 = mock(WordReserver.class);
        mod2 = mock(WordReserver.class);
        when(mod1.getName()).thenReturn(MOD_1_NAME);
        when(mod2.getName()).thenReturn(MOD_2_NAME);
        testee.handleModule(mod1);
        testee.handleModule(mod2);
    }

    @Test
    void constructorTest() {
        assertEquals(DefaultReservedWordSupporter.class.getSimpleName(), testee.getName());
    }

    @Test
    void checkModulesCompatibilityTest() {
        when(mod1.getReservedWords()).thenReturn(Set.of(RESERVED_1, RESERVED_2));
        when(mod2.getReservedWords()).thenReturn(Set.of(RESERVED_3, RESERVED_4));
        assertTrue(testee.checkModulesCompatibility());

        // Test two claiming the same
        when(mod1.getReservedWords()).thenReturn(Set.of(RESERVED_3));
        assertFalse(testee.checkModulesCompatibility());
    }

    @Test
    void getReservedWordOwnerTest() {
        when(mod1.isReservedWord(RESERVED_1)).thenReturn(true);
        when(mod1.isReservedWord(RESERVED_2)).thenReturn(true);
        when(mod2.isReservedWord(RESERVED_3)).thenReturn(true);

        assertEquals(MOD_1_NAME, testee.getReservedWordOwner(RESERVED_1));
        assertEquals(MOD_1_NAME, testee.getReservedWordOwner(RESERVED_2));
        assertEquals(MOD_2_NAME, testee.getReservedWordOwner(RESERVED_3));
        assertNull(testee.getReservedWordOwner("notReserved"));
        assertNull(testee.getReservedWordOwner(""));
        assertNull(testee.getReservedWordOwner(null));
    }

    @Test
    void getReservedWordTest() {
        when(mod1.getReservedWords()).thenReturn(Set.of(RESERVED_1, RESERVED_2));
        when(mod2.getReservedWords()).thenReturn(Set.of(RESERVED_3, RESERVED_4));

        Set<String> result = testee.getReservedWords();
        assertEquals(4, result.size());
        assertTrue(result.contains(RESERVED_1));
        assertTrue(result.contains(RESERVED_2));
        assertTrue(result.contains(RESERVED_3));
        assertTrue(result.contains(RESERVED_4));
    }
}
