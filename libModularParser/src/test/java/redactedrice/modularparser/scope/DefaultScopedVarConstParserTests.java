package redactedrice.modularparser.scope;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.LogSupporter.LogLevel;
import redactedrice.modularparser.core.ModularParser;
import redactedrice.modularparser.literal.LiteralSupporter;
import redactedrice.modularparser.reserved.ReservedWordSupporter;

public class DefaultScopedVarConstParserTests {

    private ModularParser parser;
    private DefaultScopedVarConstParser testee;
    private ScopeSupporter scopeSupporter;
    private ReservedWordSupporter rwSupporter;
    private LiteralSupporter lSupporter;

    final String TESTEE_NAME = "BasicVarConstHandler";
    final String KEYWORD = "var";
    final String SCOPE = "global";
    final String SCOPE_SUPPORTER_NAME = "TestScopeSupporter";
    private static String VAR_1 = "print";
    private static String VAR_2 = "lvar";
    private static String VAR_1_VAL = "println";
    private static String VAR_2_VAL = "local var";

    @BeforeEach
    void setup() {
        parser = mock(ModularParser.class);
        scopeSupporter = mock(ScopeSupporter.class);
        when(scopeSupporter.getName()).thenReturn(SCOPE_SUPPORTER_NAME);
        when(parser.getSupporterOfType(ScopeSupporter.class)).thenReturn(scopeSupporter);
        rwSupporter = mock(ReservedWordSupporter.class);
        when(parser.getSupporterOfType(ReservedWordSupporter.class)).thenReturn(rwSupporter);
        lSupporter = mock(LiteralSupporter.class);
        when(parser.getSupporterOfType(LiteralSupporter.class)).thenReturn(lSupporter);

        testee = spy(new DefaultScopedVarConstParser(TESTEE_NAME, true, KEYWORD));
        testee.setParser(parser);
        testee.setModuleRefs();
    }

    @Test
    void constructorSetModuleRefsTest() {
        assertEquals(TESTEE_NAME, testee.getName());
        assertEquals(KEYWORD, testee.getKeyword());
        assertEquals(scopeSupporter, testee.scopeSupporter);
        assertEquals(lSupporter, testee.literalSupporter);
        assertEquals(rwSupporter, testee.reservedWordSupporter);
    }

    @Test
    void tryParseScopedCommonTest() {
        doNothing().when(testee).addLiteral(any(), any(), any(), anyBoolean());

        assertFalse(testee.tryParseScoped(SCOPE, "none matching line", SCOPE));
        verify(testee, never()).addLiteral(any(), any(), any(), anyBoolean());

        assertFalse(testee.tryParseScoped(SCOPE, "global x = \"scope not stripped\"", SCOPE));
        verify(testee, never()).addLiteral(any(), any(), any(), anyBoolean());

        assertTrue(testee.tryParseScoped(SCOPE, "var 6bad = 42", SCOPE));
        verify(testee, never()).addLiteral(any(), any(), any(), anyBoolean());

        when(testee.ensureWordAvailableOrOwned(any(), any())).thenReturn(false);
        assertTrue(testee.tryParseScoped(SCOPE, "var x = 42", SCOPE));
        verify(testee, never()).addLiteral(any(), any(), any(), anyBoolean());
    }

    @Test
    void tryParseScopedAssignmentTest() {
        doNothing().when(testee).addLiteral(any(), any(), any(), anyBoolean());

        when(testee.ensureWordAvailableOrOwned(any(), any())).thenReturn(true);
        when(scopeSupporter.getOwner(any(), any())).thenReturn(TESTEE_NAME);
        assertTrue(testee.tryParseScoped(SCOPE, "var x = 42", SCOPE));
        verify(testee, never()).addLiteral(any(), any(), any(), anyBoolean());
        verify(testee).log(eq(LogLevel.ERROR), anyString(), any(), any(), any(), any());

        when(scopeSupporter.getOwner(any(), any())).thenReturn(null);
        assertTrue(testee.tryParseScoped("local", "var x = 42", SCOPE));
        verify(testee).addLiteral(any(), eq("local"), any(), anyBoolean());

        when(scopeSupporter.getOwner(any(), any())).thenReturn("");
        assertTrue(testee.tryParseScoped("", "var x = 42", SCOPE));
        verify(testee).addLiteral(any(), eq(SCOPE), any(), anyBoolean());
    }

    @Test
    void tryParseScopedReassignmentTest() {
        doNothing().when(testee).addLiteral(any(), any(), any(), anyBoolean());
        when(testee.ensureWordAvailableOrOwned(any(), any())).thenReturn(true);

        when(scopeSupporter.getOwner(any(), any())).thenReturn(null);
        assertTrue(testee.tryParseScoped(SCOPE, "x = 42", SCOPE));
        verify(testee, never()).addLiteral(any(), any(), any(), anyBoolean());
        verify(testee).log(eq(LogLevel.ERROR), anyString(), any(), any(), any(), any());
        clearInvocations(testee);

        when(scopeSupporter.getOwner(any(), any())).thenReturn("");
        assertTrue(testee.tryParseScoped(SCOPE, "x = 42", SCOPE));
        verify(testee, never()).addLiteral(any(), any(), any(), anyBoolean());
        verify(testee).log(eq(LogLevel.ERROR), anyString(), any(), any(), any(), any());
        clearInvocations(testee);

        when(scopeSupporter.getOwner(any(), any())).thenReturn("OtherModule");
        assertTrue(testee.tryParseScoped(SCOPE, "x = 42", SCOPE));
        verify(testee, never()).addLiteral(any(), any(), any(), anyBoolean());
        verify(testee).log(eq(LogLevel.ERROR), anyString(), any(), any(), any(), any(), any());
        clearInvocations(testee);

        when(scopeSupporter.getOwner(any(), any())).thenReturn(TESTEE_NAME);
        assertTrue(testee.tryParseScoped(SCOPE, "x = 42", SCOPE));
        verify(testee).addLiteral(any(), eq(SCOPE), any(), anyBoolean());
        clearInvocations(testee);
        clearInvocations(scopeSupporter);

        when(scopeSupporter.getNarrowestScope(any())).thenReturn(null);
        assertTrue(testee.tryParseScoped("", "x = 42", SCOPE));
        verify(testee, never()).addLiteral(any(), any(), any(), anyBoolean());
        verify(testee).log(eq(LogLevel.ERROR), anyString(), any(), any(), any());
        clearInvocations(testee);

        when(scopeSupporter.getNarrowestScope(any())).thenReturn("local");
        assertTrue(testee.tryParseScoped("", "x = 42", SCOPE));
        verify(testee).addLiteral(any(), eq("local"), any(), anyBoolean());
        clearInvocations(testee);
        clearInvocations(scopeSupporter);

        // Test with reassignment disabled
        testee = spy(new DefaultScopedVarConstParser(TESTEE_NAME, false, KEYWORD));
        testee.setParser(parser);
        testee.setModuleRefs();
        assertFalse(testee.tryParseScoped(SCOPE, "x = 42", SCOPE));
        verify(testee, never()).addLiteral(any(), any(), any(), anyBoolean());
    }

    @Test
    void addLiteralTest() {
        when(lSupporter.evaluateLiteral(any())).thenReturn(null);
        when(scopeSupporter.setData(any(), any(), any(), any())).thenReturn(false);
        testee.addLiteral("anything", SCOPE, "name", false);
        verify(scopeSupporter, never()).setData(any(), any(), any(), any());
        verify(testee).log(eq(LogLevel.ERROR), anyString(), any(), any(), any());

        when(lSupporter.evaluateLiteral(any())).thenReturn(42);
        testee.addLiteral("anything", SCOPE, "name", false);
        verify(scopeSupporter).setData(any(), any(), any(), any());
        verify(testee).log(eq(LogLevel.ERROR), anyString(), any(), any(), any());
        verify(testee, never()).log(eq(LogLevel.DEBUG), anyString(), any(), any(), any(), any(),
                any());

        when(scopeSupporter.setData(any(), any(), any(), any())).thenReturn(true);
        testee.addLiteral("anything", SCOPE, "name", false);
        verify(scopeSupporter, times(2)).setData(any(), any(), any(), any());
        verify(testee).log(eq(LogLevel.DEBUG), anyString(), eq("Changed "), any(), any(), any(),
                any());

        testee.addLiteral("anything", SCOPE, "name", true);
        verify(scopeSupporter, times(3)).setData(any(), any(), any(), any());
        verify(testee).log(eq(LogLevel.DEBUG), anyString(), eq("Added "), any(), any(), any(),
                any());
    }

    @Test
    void tryParseLiteralTest() {
        when(scopeSupporter.getData(any(), any(), any())).thenReturn(null);
        assertEquals(Optional.empty(), testee.tryParseLiteral("Any string"));

        when(scopeSupporter.getData(any(), any(), any())).thenReturn(42);
        assertEquals(Optional.of(42), testee.tryParseLiteral("Any string"));
    }

    @Test
    void setVariableTest() {
        when(scopeSupporter.setData(SCOPE, VAR_1, testee, VAR_1_VAL)).thenReturn(true);
        when(scopeSupporter.setData(SCOPE, VAR_2, testee, VAR_2_VAL)).thenReturn(false);

        assertTrue(testee.setVariable(SCOPE, VAR_1, VAR_1_VAL));
        assertFalse(testee.setVariable(SCOPE, VAR_2, VAR_2_VAL));
    }

    @Test
    void isVariableTest() {
        when(scopeSupporter.getData(Optional.empty(), VAR_1, testee)).thenReturn("not null");
        when(scopeSupporter.getData(Optional.empty(), VAR_2, testee)).thenReturn(null);

        assertTrue(testee.isVariable(VAR_1));
        assertFalse(testee.isVariable(VAR_2));
    }

    @Test
    void getVariableValueTest() {
        when(scopeSupporter.getData(Optional.empty(), VAR_1, testee)).thenReturn("object");
        when(scopeSupporter.getData(Optional.empty(), VAR_2, testee)).thenReturn(null);

        assertEquals("object", testee.getVariableValue(VAR_1));
        assertNull(testee.getVariableValue(VAR_2));
    }

    @Test
    void getVariablesTest() {
        Set<String> names = Set.of(VAR_1, VAR_2);
        when(scopeSupporter.getAllOwnedNames(any(), any())).thenReturn(names);

        Set<String> results = testee.getVariables();
        assertEquals(2, results.size());
        assertTrue(results.contains(VAR_1));
        assertTrue(results.contains(VAR_2));
    }
}
