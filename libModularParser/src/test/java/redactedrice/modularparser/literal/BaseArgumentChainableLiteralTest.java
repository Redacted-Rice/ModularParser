package redactedrice.modularparser.literal;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.ModularParser;
import redactedrice.modularparser.core.Response;
import redactedrice.modularparser.lineformer.Grouper;
import redactedrice.modularparser.testsupport.SimpleObject;
import redactedrice.modularparser.testsupport.SimpleObjectLiteralParser;

class BaseArgumentChainableLiteralTest {

    private ModularParser parser;
    private LiteralSupporter literalSupporter;
    private Grouper grouper;
    private BaseArgumentChainableLiteral testee;

    static final String CHAINED_ARG = "so";

    @BeforeEach
    void setup() {
        parser = mock(ModularParser.class);
        literalSupporter = mock(LiteralSupporter.class);
        grouper = mock(Grouper.class);
        when(parser.getSupporterOfType(LiteralSupporter.class)).thenReturn(literalSupporter);

        testee = new SimpleObjectLiteralParser(grouper);
        testee.setParser(parser);
        testee.setModuleRefs();
    }

    @Test
    void defaultGrouper() {
        // Ensure default is null to keep order of tests from mattering
        BaseArgumentChainableLiteral.setDefaultGrouper(null);
        assertNull(BaseArgumentChainableLiteral.getDefaultGrouper());

        BaseArgumentChainableLiteral.setDefaultGrouper(grouper);
        assertEquals(grouper, BaseArgumentChainableLiteral.getDefaultGrouper());
        BaseArgumentChainableLiteral defaultGrouper = new SimpleObjectLiteralParser();
        assertEquals(grouper, defaultGrouper.getGrouper());

        // Set it back to null for other tests and test that constructor ensures not null
        BaseArgumentChainableLiteral.setDefaultGrouper(null);
        assertThrows(IllegalArgumentException.class, SimpleObjectLiteralParser::new);
    }

    @Test
    void tryEvaluateChainedLiteral() {
        final SimpleObject baseObj = new SimpleObject(1, true, "baseSo", null);

        assertEquals(Response.notHandled(),
                testee.tryEvaluateChainedLiteral(baseObj, "something invalid"));

        when(grouper.tryGetNextGroup(any(), anyBoolean()))
                .thenReturn(Response.is(new String[] {"", "5", ""}));
        when(grouper.hasOpenGroup(any())).thenReturn(false);
        when(grouper.isEmptyGroup(any())).thenReturn(false);
        when(literalSupporter.evaluateLiteral(any())).thenReturn(Response.is(5));
        Response<Object> res = testee.tryEvaluateChainedLiteral(baseObj, "SimpleObject (5)");
        assertTrue(res.wasValueReturned());
        assertEquals(5, ((SimpleObject) res.getValue()).getInt());
        assertEquals(baseObj, ((SimpleObject) res.getValue()).getSo());
    }
}
