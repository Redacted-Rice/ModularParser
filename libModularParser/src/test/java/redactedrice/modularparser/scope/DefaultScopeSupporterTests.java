package redactedrice.modularparser.scope;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
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
}
