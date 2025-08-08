package redactedrice.modularparser.reserved;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.ModularParser;
import redactedrice.modularparser.reserved.ReservedWordSupporter.ReservedType;

public class DefaultReservedWordSupporterTests {

    final String RESERVED_1 = "alias";
    final String RESERVED_2 = "var";
    final String RESERVED_3 = "const";
    final String RESERVED_4 = "scope";

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
        testee.handleModule(mod1);
        testee.handleModule(mod2);
    }

    @Test
    void constructorTest() {
        assertEquals("DefaultReservedWordSupporter", testee.getName());
    }

    private void setupGetReservedWords() {
        when(mod1.getReservedWords(ReservedType.EXCLUSIVE))
                .thenReturn(new HashSet<String>(Arrays.asList(RESERVED_1)));
        when(mod1.getReservedWords(ReservedType.SHAREABLE))
                .thenReturn(new HashSet<String>(Arrays.asList(RESERVED_2)));

        when(mod2.getReservedWords(ReservedType.SHAREABLE))
                .thenReturn(new HashSet<String>(Arrays.asList(RESERVED_2)));
        when(mod2.getReservedWords(ReservedType.EXCLUSIVE))
                .thenReturn(new HashSet<String>(Arrays.asList(RESERVED_3)));
        when(mod2.getReservedWords(ReservedType.SHAREABLE))
                .thenReturn(new HashSet<String>(Arrays.asList(RESERVED_4)));
    }

    private void setupGetAllReservedWords() {
        when(mod1.getAllReservedWords()).thenReturn(
                Map.of(RESERVED_1, ReservedType.EXCLUSIVE, RESERVED_2, ReservedType.SHAREABLE));
        when(mod2.getAllReservedWords()).thenReturn(Map.of(RESERVED_2, ReservedType.SHAREABLE,
                RESERVED_3, ReservedType.EXCLUSIVE, RESERVED_4, ReservedType.SHAREABLE));
    }

    @Test
    void checkModulesCompatibilityTest() {
        setupGetReservedWords();
        setupGetAllReservedWords();

        assertTrue(testee.checkModulesCompatibility());

        // Test two claiming the same exclusively
        when(mod1.getReservedWords(ReservedType.EXCLUSIVE))
                .thenReturn(new HashSet<String>(Arrays.asList(RESERVED_3)));
        assertFalse(testee.checkModulesCompatibility());

        // Revert the behavior and try to claim a shareable word
        when(mod1.getReservedWords(ReservedType.EXCLUSIVE))
                .thenReturn(new HashSet<String>(Arrays.asList(RESERVED_1)));
        when(mod2.getReservedWords(ReservedType.EXCLUSIVE))
                .thenReturn(new HashSet<String>(Arrays.asList(RESERVED_2)));
        assertFalse(testee.checkModulesCompatibility());
    }

    @Test
    void isReservedWordTest() {
        when(mod1.isReservedWord(RESERVED_1, Optional.empty())).thenReturn(true);
        when(mod1.isReservedWord(RESERVED_2, Optional.empty())).thenReturn(true);
        when(mod1.isReservedWord(RESERVED_1, Optional.of(ReservedType.EXCLUSIVE))).thenReturn(true);
        when(mod1.isReservedWord(RESERVED_2, Optional.of(ReservedType.SHAREABLE))).thenReturn(true);

        when(mod2.isReservedWord(RESERVED_2, Optional.empty())).thenReturn(true);
        when(mod2.isReservedWord(RESERVED_3, Optional.empty())).thenReturn(true);
        when(mod2.isReservedWord(RESERVED_2, Optional.of(ReservedType.SHAREABLE))).thenReturn(true);
        when(mod2.isReservedWord(RESERVED_3, Optional.of(ReservedType.EXCLUSIVE))).thenReturn(true);

        assertTrue(testee.isReservedWord(RESERVED_1));
        assertTrue(testee.isReservedWord(RESERVED_2));
        assertTrue(testee.isReservedWord(RESERVED_3));
        assertFalse(testee.isReservedWord("notReserved"));
        assertFalse(testee.isReservedWord(""));
        assertFalse(testee.isReservedWord(null));

        assertTrue(testee.isReservedWord(RESERVED_1, Optional.of(ReservedType.EXCLUSIVE)));
        assertTrue(testee.isReservedWord(RESERVED_2, Optional.of(ReservedType.SHAREABLE)));
        assertTrue(testee.isReservedWord(RESERVED_3, Optional.of(ReservedType.EXCLUSIVE)));
        assertFalse(testee.isReservedWord(RESERVED_1, Optional.of(ReservedType.SHAREABLE)));
        assertFalse(testee.isReservedWord(RESERVED_2, Optional.of(ReservedType.EXCLUSIVE)));
        assertFalse(testee.isReservedWord(RESERVED_3, Optional.of(ReservedType.SHAREABLE)));

    }

    @Test
    void getReservedWordTest() {
        setupGetReservedWords();

        Set<String> result = testee.getReservedWords(ReservedType.EXCLUSIVE);
        assertEquals(2, result.size());
        assertTrue(result.contains(RESERVED_1));
        assertTrue(result.contains(RESERVED_3));

        result = testee.getReservedWords(ReservedType.SHAREABLE);
        assertEquals(2, result.size());
        assertTrue(result.contains(RESERVED_2));
        assertTrue(result.contains(RESERVED_4));
    }

    @Test
    void getAllReservedWordTest() {
        setupGetAllReservedWords();

        Map<String, ReservedType> result = testee.getAllReservedWords();
        assertEquals(4, result.size());
        assertEquals(ReservedType.EXCLUSIVE, result.get(RESERVED_1));
        assertEquals(ReservedType.SHAREABLE, result.get(RESERVED_2));
        assertEquals(ReservedType.EXCLUSIVE, result.get(RESERVED_3));
        assertEquals(ReservedType.SHAREABLE, result.get(RESERVED_4));
    }
}
