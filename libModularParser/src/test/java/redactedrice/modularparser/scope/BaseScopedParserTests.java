package redactedrice.modularparser.scope;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.ModularParser;

class BaseScopedParserTests {
    final String NAME = "ScopedParser";
    final String KEYWORD = "foo";

    private BaseScopedKeywordParser testee;
    private ModularParser parser;
    private ScopeSupporter scopeSupporter;

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
        testee.setParser(parser);
    }

    @Test
    void constructorSetterTest() {
        assertEquals(NAME, testee.getName());
        assertEquals(KEYWORD, testee.getKeyword());
    }

    @Test
    void setModuleRefsTest() {
        testee.setModuleRefs();
        assertEquals(scopeSupporter, testee.scopeSupporter);
    }
}
