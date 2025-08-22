package redactedrice.modularparser.scope;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.ModularParser;
import redactedrice.modularparser.reserved.ReservedWordSupporter;

public class DefaultScopeAliasParserTests {

    private ModularParser parser;
    private DefaultScopedAliasParser testee;
    private ScopeSupporter scopeSupporter;
    private ReservedWordSupporter rwSupporter;

    final String TESTEE_NAME = "BasicAliasHandler";
    final String SCOPE = "global";
    final String SCOPE_SUPPORTER_NAME = "TestScopeSupporter";
    private static String ALIAS_1 = "print";
    private static String ALIAS_2 = "lvar";
    private static String ALIAS_1_VAL = "println";
    private static String ALIAS_2_VAL = "local var";

    @BeforeEach
    void setup() {
        parser = mock(ModularParser.class);
        scopeSupporter = mock(ScopeSupporter.class);
        when(scopeSupporter.getName()).thenReturn(SCOPE_SUPPORTER_NAME);
        when(parser.getSupporterOfType(ScopeSupporter.class)).thenReturn(scopeSupporter);
        rwSupporter = mock(ReservedWordSupporter.class);
        when(parser.getSupporterOfType(ReservedWordSupporter.class)).thenReturn(rwSupporter);

        testee = spy(new DefaultScopedAliasParser());
        testee.setParser(parser);
        testee.setModuleRefs();
    }

    @Test
    void constructorTest() {
        assertEquals(TESTEE_NAME, testee.getName());
        assertEquals("alias", testee.getKeyword());
    }

    @Test
    void isValidNameTest() {
        assertFalse(DefaultScopedAliasParser.isValidName(null));
        assertFalse(DefaultScopedAliasParser.isValidName(" "));
        assertFalse(DefaultScopedAliasParser.isValidName("9var"));
        assertFalse(DefaultScopedAliasParser.isValidName("test space"));
        assertFalse(DefaultScopedAliasParser.isValidName("test.period"));
        assertFalse(DefaultScopedAliasParser.isValidName("test-hyphen"));

        assertTrue(DefaultScopedAliasParser.isValidName("var"));
        assertTrue(DefaultScopedAliasParser.isValidName("v2"));
        assertTrue(DefaultScopedAliasParser.isValidName("v"));
        assertTrue(DefaultScopedAliasParser.isValidName("v_with_underscore"));
        assertTrue(DefaultScopedAliasParser.isValidName("Var"));
        assertTrue(DefaultScopedAliasParser.isValidName("vAR"));
    }

    @Test
    void lineContinuersValidLineHasOpenModifierTest() {
        assertTrue(testee.lineContinuersValid("any string", true));
        assertFalse(testee.lineHasOpenModifier("any string"));
    }

    @Test
    void modifyLineTest() {
        when(scopeSupporter.getAllOwnedData(any(), any()))
                .thenReturn(Map.of(ALIAS_1, ALIAS_1_VAL, ALIAS_2, ALIAS_2_VAL));

        final String NO_ALIAS = "This is a test line";
        assertEquals(NO_ALIAS, testee.modifyLine(NO_ALIAS));

        assertEquals("println local var test local var println",
                testee.modifyLine("print lvar test lvar print"));
    }

    @Test
    void tryParseScopedTest() {
        when(rwSupporter.getReservedWordOwner(any())).thenReturn(null);
        when(scopeSupporter.setData(any(), any(), any(), any())).thenReturn(true);

        assertFalse(testee.tryParseScoped(SCOPE, "none matching line", SCOPE));
        verify(scopeSupporter, never()).setData(any(), any(), any(), any());

        assertTrue(testee.tryParseScoped(SCOPE, "alias 6bad = println", SCOPE));
        verify(scopeSupporter, never()).setData(any(), any(), any(), any());

        assertTrue(testee.tryParseScoped(SCOPE, "alias print = println", SCOPE));
        verify(scopeSupporter).setData(eq(SCOPE), any(), any(), any());
        clearInvocations(scopeSupporter);

        assertTrue(testee.tryParseScoped("", "alias print = 'println'", SCOPE));
        verify(scopeSupporter).setData(eq(SCOPE), any(), any(), any());
        clearInvocations(scopeSupporter);

        assertTrue(testee.tryParseScoped(SCOPE, "alias print = \"println\"", SCOPE));
        verify(scopeSupporter).setData(eq(SCOPE), any(), any(), any());
        clearInvocations(scopeSupporter);

        assertTrue(testee.tryParseScoped(SCOPE, "alias bad = 'println", SCOPE));
        assertTrue(testee.tryParseScoped(SCOPE, "alias bad = \"println", SCOPE));
        assertTrue(testee.tryParseScoped(SCOPE, "alias bad = println'", SCOPE));
        assertTrue(testee.tryParseScoped(SCOPE, "alias bad = println\"", SCOPE));
        verify(scopeSupporter, never()).setData(any(), any(), any(), any());

        when(scopeSupporter.setData(any(), any(), any(), any())).thenReturn(false);
        assertTrue(testee.tryParseScoped(SCOPE, "alias print = \"println\"", SCOPE));
    }

    @Test
    void tryParseScopedNameConflictTest() {
        final String OTHER_OWNER = "SomeModule";
        when(scopeSupporter.setData(any(), any(), any(), any())).thenReturn(true);

        // Other module reserved the word
        when(rwSupporter.getReservedWordOwner(any())).thenReturn(OTHER_OWNER);
        assertTrue(testee.tryParseScoped(SCOPE, "alias print = println", SCOPE));
        verify(scopeSupporter, never()).setData(any(), any(), any(), any());
        verify(testee).log(any(), anyString(), eq("print"), eq(OTHER_OWNER));

        // Other module already defined this word
        when(rwSupporter.getReservedWordOwner(any())).thenReturn(SCOPE_SUPPORTER_NAME);
        when(scopeSupporter.getOwner(any(), any())).thenReturn(OTHER_OWNER);
        assertTrue(testee.tryParseScoped(SCOPE, "alias print = println", SCOPE));
        verify(testee).log(any(), anyString(), eq("print"), eq(SCOPE), eq(OTHER_OWNER));
        verify(scopeSupporter, never()).setData(any(), any(), any(), any());

        // this module already defined this word
        when(scopeSupporter.getOwner(any(), any())).thenReturn(TESTEE_NAME);
        assertTrue(testee.tryParseScoped(SCOPE, "alias print = println", SCOPE));
        verify(testee).log(any(), anyString(), eq("print"), eq(SCOPE), eq(TESTEE_NAME));
        verify(scopeSupporter, never()).setData(any(), any(), any(), any());

        // Already defined but in a different scope. This is okay
        when(scopeSupporter.getOwner(any(), any())).thenReturn(null);
        assertTrue(testee.tryParseScoped(SCOPE, "alias print = println", SCOPE));
        verify(scopeSupporter).setData(any(), any(), any(), any());
    }

    @Test
    void setAliasTest() {
        when(scopeSupporter.setData(SCOPE, ALIAS_1, testee, ALIAS_1_VAL)).thenReturn(true);
        when(scopeSupporter.setData(SCOPE, ALIAS_2, testee, ALIAS_2_VAL)).thenReturn(false);

        assertTrue(testee.setAlias(SCOPE, ALIAS_1, ALIAS_1_VAL));
        assertFalse(testee.setAlias(SCOPE, ALIAS_2, ALIAS_2_VAL));
    }

    @Test
    void isAlias() {
        when(scopeSupporter.getData(Optional.empty(), ALIAS_1, testee)).thenReturn("not null");
        when(scopeSupporter.getData(Optional.empty(), ALIAS_2, testee)).thenReturn(null);

        assertTrue(testee.isAlias(ALIAS_1));
        assertFalse(testee.isAlias(ALIAS_2));
    }

    @Test
    void getAllAliasesTest() {
        Set<String> names = Set.of(ALIAS_1, ALIAS_2);
        when(scopeSupporter.getAllOwnedNames(any(), any())).thenReturn(names);

        Set<String> results = testee.getAliases();
        assertEquals(2, results.size());
        assertTrue(results.contains(ALIAS_1));
        assertTrue(results.contains(ALIAS_2));
    }
}
