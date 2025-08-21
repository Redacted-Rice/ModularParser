package redactedrice.modularparser.alias;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DefaultAliasSupporterTests {

    private static String MOD1_NAME = "AliasParser1";
    private static String MOD2_NAME = "AliasParser2";

    private static String ALIAS_1 = "print";
    private static String ALIAS_2 = "println";
    private static String ALIAS_3 = "printerr";
    private static String ALIAS_4 = "printerrln";

    private DefaultAliasSupporter testee;
    AliasParser mod1;
    AliasParser mod2;

    @BeforeEach
    void setup() {
        mod1 = mock(AliasParser.class);
        mod2 = mock(AliasParser.class);

        when(mod1.getName()).thenReturn(MOD1_NAME);
        when(mod2.getName()).thenReturn(MOD2_NAME);
    }

    @Test
    void constructorTest() {
        testee = new DefaultAliasSupporter();
        assertEquals("AliasSupportModule", testee.getName());
    }

    @Test
    void isAliasDefinedTest() {
        testee = new DefaultAliasSupporter();
        testee.handleModule(mod1);
        testee.handleModule(mod2);

        when(mod1.isAlias(ALIAS_1)).thenReturn(true);
        when(mod1.isAlias(ALIAS_2)).thenReturn(false);
        when(mod1.isAlias(ALIAS_3)).thenReturn(false);
        when(mod2.isAlias(ALIAS_1)).thenReturn(false);
        when(mod2.isAlias(ALIAS_2)).thenReturn(true);
        when(mod2.isAlias(ALIAS_3)).thenReturn(false);

        assertTrue(testee.isAliasDefined(ALIAS_1));
        assertTrue(testee.isAliasDefined(ALIAS_2));
        assertFalse(testee.isAliasDefined(ALIAS_3));

        verify(mod1).isAlias(ALIAS_1);
        verify(mod1).isAlias(ALIAS_2);
        verify(mod1).isAlias(ALIAS_3);
        verify(mod2, never()).isAlias(ALIAS_1);
        verify(mod2).isAlias(ALIAS_2);
        verify(mod2).isAlias(ALIAS_3);
    }

    @Test
    void getAllAliasesTest() {
        testee = new DefaultAliasSupporter();
        testee.handleModule(mod1);
        testee.handleModule(mod2);

        when(mod1.getAliases()).thenReturn(Set.of(ALIAS_1, ALIAS_2));
        when(mod2.getAliases()).thenReturn(Set.of(ALIAS_2, ALIAS_3));

        Set<String> results = testee.getAllAliases();
        assertEquals(3, results.size());
        assertTrue(results.contains(ALIAS_1));
        assertTrue(results.contains(ALIAS_2));
        assertTrue(results.contains(ALIAS_3));
        assertFalse(results.contains(ALIAS_4));
    }
}
