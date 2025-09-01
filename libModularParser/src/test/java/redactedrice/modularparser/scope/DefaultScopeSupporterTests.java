package redactedrice.modularparser.scope;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.ModularParser;
import redactedrice.modularparser.core.Module;

public class DefaultScopeSupporterTests {

    private static String NAME = "ScopeSupporter";
    private static String MOD1_NAME = "ScopedParser1";
    private static String MOD2_NAME = "ScopedParser2";
    private static String SCOPE1 = "global";
    private static String SCOPE2 = "file";

    private ModularParser parser;
    private DefaultScopeSupporter testee;
    ScopedParser mod1;
    ScopedParser mod2;

    @BeforeEach
    void setup() {
        parser = mock(ModularParser.class);
        mod1 = mock(ScopedParser.class);
        mod2 = mock(ScopedParser.class);

        when(mod1.getName()).thenReturn(MOD1_NAME);
        when(mod2.getName()).thenReturn(MOD2_NAME);
    }

    @Test
    void constructorTest() {
        testee = new DefaultScopeSupporter(NAME, true);
        assertEquals(NAME, testee.getName());
        assertEquals(true, testee.allowImplicit);

        testee = new DefaultScopeSupporter(NAME, false);
        assertEquals(NAME, testee.getName());
        assertEquals(false, testee.allowImplicit);
    }

    @Test
    void handleModuleTest() {
        testee = new DefaultScopeSupporter(NAME, true);
        testee.setParser(parser);
        testee.scopeOrder.push(SCOPE1);
        testee.scopeOrder.push(SCOPE2);

        Module modOther = mock(Module.class);

        testee.handleModule(mod1);
        testee.handleModule(mod2);
        testee.handleModule(modOther);

        assertEquals(2, testee.parsers.size());
        assertTrue(testee.parsers.contains(mod1));
        assertTrue(testee.parsers.contains(mod2));
        assertTrue(testee.ownerMap.containsKey(MOD1_NAME));
        assertTrue(testee.ownerMap.containsKey(MOD2_NAME));

        Map<String, Set<String>> ownerScopeMap = testee.ownerMap.get(MOD1_NAME);
        assertEquals(2, ownerScopeMap.size());
        assertTrue(ownerScopeMap.containsKey(SCOPE1));
        assertTrue(ownerScopeMap.containsKey(SCOPE2));
    }

    @Test
    void tryParseLineTest() {
        testee = spy(new DefaultScopeSupporter(NAME, true));
        testee.setParser(parser);
        testee.scopeOrder.push(SCOPE1);
        testee.scopeOrder.push(SCOPE2);
        testee.handleModule(mod1);
        testee.handleModule(mod2);

        final String SCOPE = "global";
        final String REST_OF_LINE = "var x = 5";
        final String LINE_1 = SCOPE + " " + REST_OF_LINE;
        doReturn(null).when(testee).splitScope(LINE_1);
        assertFalse(testee.tryParseLine(LINE_1));
        doReturn(new String[] {}).when(testee).splitScope(LINE_1);
        assertFalse(testee.tryParseLine(LINE_1));

        doReturn(new String[] {SCOPE, REST_OF_LINE}).when(testee).splitScope(LINE_1);
        when(testee.currentScope()).thenReturn(SCOPE);
        when(mod1.tryParseScoped(SCOPE, REST_OF_LINE, SCOPE)).thenReturn(false);
        when(mod2.tryParseScoped(SCOPE, REST_OF_LINE, SCOPE)).thenReturn(false);
        assertFalse(testee.tryParseLine(LINE_1));
        verify(mod1).tryParseScoped(SCOPE, REST_OF_LINE, SCOPE);
        verify(mod2).tryParseScoped(SCOPE, REST_OF_LINE, SCOPE);

        when(mod2.tryParseScoped(SCOPE, REST_OF_LINE, SCOPE)).thenReturn(true);
        assertTrue(testee.tryParseLine(LINE_1));
        verify(mod1, times(2)).tryParseScoped(SCOPE, REST_OF_LINE, SCOPE);
        verify(mod2, times(2)).tryParseScoped(SCOPE, REST_OF_LINE, SCOPE);

        when(mod1.tryParseScoped(SCOPE, REST_OF_LINE, SCOPE)).thenReturn(true);
        assertTrue(testee.tryParseLine(LINE_1));
        verify(mod1, times(3)).tryParseScoped(SCOPE, REST_OF_LINE, SCOPE);
        verify(mod2, times(2)).tryParseScoped(SCOPE, REST_OF_LINE, SCOPE);
    }

    @Test
    void pushPopRemoveCurrentScopeTest() {
        testee = spy(new DefaultScopeSupporter(NAME, true));
        testee.setParser(parser);
        testee.handleModule(mod1);
        testee.handleModule(mod2);

        // No scope
        assertNull(testee.currentScope());

        // Push first
        assertTrue(testee.pushScope(SCOPE1));
        assertEquals(1, testee.scopeOrder.size());
        assertTrue(testee.scopeOrder.contains(SCOPE1));

        assertEquals(1, testee.scopedVals.size());
        assertTrue(testee.scopedVals.containsKey(SCOPE1));
        assertEquals(1, testee.ownerMap.get(MOD1_NAME).size());
        assertTrue(testee.ownerMap.get(MOD1_NAME).containsKey(SCOPE1));
        assertEquals(1, testee.ownerMap.get(MOD2_NAME).size());
        assertTrue(testee.ownerMap.get(MOD2_NAME).containsKey(SCOPE1));

        assertEquals(SCOPE1, testee.currentScope());

        // Push already existing
        assertFalse(testee.pushScope(SCOPE1));
        assertEquals(1, testee.scopeOrder.size());
        assertTrue(testee.scopeOrder.contains(SCOPE1));

        assertEquals(1, testee.scopedVals.size());
        assertEquals(1, testee.ownerMap.get(MOD1_NAME).size());
        assertEquals(1, testee.ownerMap.get(MOD2_NAME).size());

        assertEquals(SCOPE1, testee.currentScope());

        // push second
        assertTrue(testee.pushScope(SCOPE2));
        assertEquals(2, testee.scopeOrder.size());
        assertTrue(testee.scopeOrder.contains(SCOPE1));
        assertTrue(testee.scopeOrder.contains(SCOPE2));

        assertEquals(2, testee.scopedVals.size());
        assertTrue(testee.scopedVals.containsKey(SCOPE1));
        assertTrue(testee.scopedVals.containsKey(SCOPE2));
        assertEquals(2, testee.ownerMap.get(MOD1_NAME).size());
        assertTrue(testee.ownerMap.get(MOD1_NAME).containsKey(SCOPE1));
        assertTrue(testee.ownerMap.get(MOD1_NAME).containsKey(SCOPE2));
        assertEquals(2, testee.ownerMap.get(MOD2_NAME).size());
        assertTrue(testee.ownerMap.get(MOD2_NAME).containsKey(SCOPE1));
        assertTrue(testee.ownerMap.get(MOD2_NAME).containsKey(SCOPE2));

        assertEquals(SCOPE2, testee.currentScope());

        // Remove non-exisitng
        assertFalse(testee.removeScope("badScope"));
        assertEquals(2, testee.scopeOrder.size());

        // remove first scope
        assertTrue(testee.removeScope(SCOPE1));
        assertEquals(1, testee.scopeOrder.size());
        assertTrue(testee.scopeOrder.contains(SCOPE2));

        assertEquals(1, testee.scopedVals.size());
        assertTrue(testee.scopedVals.containsKey(SCOPE2));
        assertEquals(1, testee.ownerMap.get(MOD1_NAME).size());
        assertTrue(testee.ownerMap.get(MOD1_NAME).containsKey(SCOPE2));
        assertEquals(1, testee.ownerMap.get(MOD2_NAME).size());
        assertTrue(testee.ownerMap.get(MOD2_NAME).containsKey(SCOPE2));

        assertEquals(SCOPE2, testee.currentScope());

        // push it back
        assertTrue(testee.pushScope(SCOPE1));
        assertEquals(SCOPE1, testee.currentScope());

        // pop scope
        assertTrue(testee.popScope());
        assertEquals(SCOPE2, testee.currentScope());
        assertEquals(1, testee.scopeOrder.size());
        assertTrue(testee.scopeOrder.contains(SCOPE2));

        assertEquals(1, testee.scopedVals.size());
        assertTrue(testee.scopedVals.containsKey(SCOPE2));
        assertEquals(1, testee.ownerMap.get(MOD1_NAME).size());
        assertTrue(testee.ownerMap.get(MOD1_NAME).containsKey(SCOPE2));
        assertEquals(1, testee.ownerMap.get(MOD2_NAME).size());
        assertTrue(testee.ownerMap.get(MOD2_NAME).containsKey(SCOPE2));

        assertEquals(SCOPE2, testee.currentScope());

        // Pop last scope
        assertTrue(testee.popScope());
        assertNull(testee.currentScope());

        // Pop with none remaining
        assertFalse(testee.popScope());
    }

    @Test
    void splitScopeTest() {
        final String UNUSED_SCOPE = "unusedScope";
        final String LINE = "var x = 42";
        final String SCOPE1_LINE = SCOPE1 + " " + LINE;
        final String SCOPE2_LINE = SCOPE2 + " " + LINE;
        final String UNUSED_SCOPE_LINE = UNUSED_SCOPE + " " + LINE;

        // allow implicit
        testee = new DefaultScopeSupporter(NAME, true);
        testee.setParser(parser);
        testee.scopeOrder.push(SCOPE1);
        testee.scopeOrder.push(SCOPE2);

        String[] result = testee.splitScope(SCOPE1_LINE);
        assertEquals(2, result.length);
        assertEquals(SCOPE1, result[0]);
        assertEquals(LINE, result[1]);

        result = testee.splitScope(SCOPE2_LINE);
        assertEquals(2, result.length);
        assertEquals(SCOPE2, result[0]);
        assertEquals(LINE, result[1]);

        result = testee.splitScope(LINE);
        assertEquals(2, result.length);
        assertTrue(result[0].isEmpty());
        assertEquals(LINE, result[1]);

        // None existent scope - will be considered implicit
        result = testee.splitScope(UNUSED_SCOPE_LINE);
        assertEquals(2, result.length);
        assertTrue(result[0].isEmpty());
        assertEquals(UNUSED_SCOPE_LINE, result[1]);

        // Now try without implicit
        testee = new DefaultScopeSupporter(NAME, false);
        testee.setParser(parser);
        testee.scopeOrder.push(SCOPE1);
        testee.scopeOrder.push(SCOPE2);

        result = testee.splitScope(SCOPE1_LINE);
        assertEquals(2, result.length);
        assertEquals(SCOPE1, result[0]);
        assertEquals(LINE, result[1]);

        result = testee.splitScope(SCOPE2_LINE);
        assertEquals(2, result.length);
        assertEquals(SCOPE2, result[0]);
        assertEquals(LINE, result[1]);

        result = testee.splitScope(LINE);
        assertNull(result);

        // None existent scope - will be returned null
        result = testee.splitScope(UNUSED_SCOPE_LINE);
        assertNull(result);
    }

    @Test
    void reservedWordTest() {
        testee = new DefaultScopeSupporter(NAME, true);

        final String WORD1 = "foo";
        final String WORD2 = "bar";
        final String WORD3 = "foobar";
        testee.scopedVals.put(SCOPE1,
                Map.of(WORD1, new OwnedObject("", null), WORD2, new OwnedObject("", null)));
        testee.scopedVals.put(SCOPE2,
                Map.of(WORD2, new OwnedObject("", null), WORD3, new OwnedObject("", null)));

        assertTrue(testee.isReservedWord(WORD1));
        assertTrue(testee.isReservedWord(WORD2));
        assertTrue(testee.isReservedWord(WORD3));
        assertFalse(testee.isReservedWord("other"));
        assertFalse(testee.isReservedWord("barfoo"));

        Set<String> results = testee.getReservedWords();
        assertEquals(3, results.size());
        assertTrue(results.contains(WORD1));
        assertTrue(results.contains(WORD2));
        assertTrue(results.contains(WORD3));
    }
}
