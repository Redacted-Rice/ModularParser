package redactedrice.modularparser.scope;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.LogSupporter.LogLevel;
import redactedrice.modularparser.core.ModularParser;
import redactedrice.modularparser.reserved.ReservedWordSupporter;

class BaseScopedParserTests {
    static final String NAME = "ScopedParser";
    static final String KEYWORD = "var";
    static final String VAR_NAME = "bar";
    static final String SCOPE_NAME = "ScopeSupporter";

    private BaseScopedKeywordParser testee;
    private ModularParser parser;
    private ScopeSupporter scopeSupporter;
    private ReservedWordSupporter rwSupporter;

    private class BaseScopedParserTester extends BaseScopedKeywordParser {
        protected BaseScopedParserTester(String name) {
            super(name, KEYWORD);
        }

        @Override
        public boolean tryParseScoped(String scope, String logicalLine, String defaultScope) {
            return false;
        }
    }

    @BeforeEach
    void setup() {
        testee = spy(new BaseScopedParserTester(NAME));
        parser = mock(ModularParser.class);
        scopeSupporter = mock(ScopeSupporter.class);
        when(parser.getSupporterOfType(ScopeSupporter.class)).thenReturn(scopeSupporter);
        rwSupporter = mock(ReservedWordSupporter.class);
        when(parser.getSupporterOfType(ReservedWordSupporter.class)).thenReturn(rwSupporter);
        testee.setParser(parser);
        testee.setModuleRefs();
    }

    @Test
    void constructorSetModuleRefsTest() {
        assertEquals(NAME, testee.getName());
        assertEquals(KEYWORD, testee.getKeyword());
        assertEquals(scopeSupporter, testee.scopeSupporter);
        assertEquals(rwSupporter, testee.reservedWordSupporter);
    }

    @Test
    void isValidNameTest() {
        assertFalse(BaseScopedKeywordParser.isValidName(null));
        assertFalse(BaseScopedKeywordParser.isValidName(""));
        assertFalse(BaseScopedKeywordParser.isValidName("    "));
        assertFalse(BaseScopedKeywordParser.isValidName("9var"));
        assertFalse(BaseScopedKeywordParser.isValidName("test space"));
        assertFalse(BaseScopedKeywordParser.isValidName("test.period"));
        assertFalse(BaseScopedKeywordParser.isValidName("test-hyphen"));

        assertTrue(BaseScopedKeywordParser.isValidName("var"));
        assertTrue(BaseScopedKeywordParser.isValidName("v2"));
        assertTrue(BaseScopedKeywordParser.isValidName("v"));
        assertTrue(BaseScopedKeywordParser.isValidName("v_with_underscore"));
        assertTrue(BaseScopedKeywordParser.isValidName("Var"));
        assertTrue(BaseScopedKeywordParser.isValidName("vAR"));
    }

    @Test
    void ensureWordAvailableOrOwnedTest() {
        when(rwSupporter.getReservedWordOwner(any())).thenReturn(null);
        assertTrue(testee.ensureWordAvailableOrOwned(VAR_NAME));

        when(rwSupporter.getReservedWordOwner(any())).thenReturn("DifferentOwner");
        when(scopeSupporter.getName()).thenReturn(SCOPE_NAME);
        assertFalse(testee.ensureWordAvailableOrOwned(VAR_NAME));
        verify(testee).log(eq(LogLevel.ERROR), anyString(), any(), any());

        when(rwSupporter.getReservedWordOwner(any())).thenReturn(SCOPE_NAME);
        assertTrue(testee.ensureWordAvailableOrOwned(VAR_NAME));
    }
}
