package redactedrice.modularparser.literal.argumented;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.ModularParser;
import redactedrice.modularparser.core.Response;
import redactedrice.modularparser.lineformer.Grouper;
import redactedrice.modularparser.literal.LiteralSupporter;
import redactedrice.modularparser.literal.argumented.BaseArgumentedChainableLiteral;
import redactedrice.modularparser.testsupport.SimpleObject;
import redactedrice.modularparser.testsupport.SimpleObjectLiteralParser;

class BaseArgumentChainableLiteralTest {

    private ModularParser parser;
    private LiteralSupporter literalSupporter;
    private Grouper grouper;
    private BaseArgumentedChainableLiteral testee;

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
        BaseArgumentedChainableLiteral.setDefaultGrouper(null);
        assertNull(BaseArgumentedChainableLiteral.getDefaultGrouper());

        BaseArgumentedChainableLiteral.setDefaultGrouper(grouper);
        assertEquals(grouper, BaseArgumentedChainableLiteral.getDefaultGrouper());
        BaseArgumentedChainableLiteral defaultGrouper = new SimpleObjectLiteralParser();
        assertEquals(grouper, defaultGrouper.getGrouper());

        // Set it back to null for other tests and test that constructor ensures not null
        BaseArgumentedChainableLiteral.setDefaultGrouper(null);
        assertThrows(IllegalArgumentException.class, SimpleObjectLiteralParser::new);
    }
    
    @Test
    void constuctor() {
        assertEquals(SimpleObjectLiteralParser.class.getSimpleName(), testee.getName());
        assertEquals("so", testee.getChainedArg());
        assertEquals("simpleobject", testee.getKeyword());
        assertEquals(grouper, testee.getGrouper());
        assertEquals(1, testee.getRequiredArgs().length);
        assertEquals(4, testee.getOptionalArgs().length);
        assertEquals(4, testee.getOptionalDefaults().length);
        assertEquals(literalSupporter, testee.getLiteralSupporter());
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

    @Test
    void handlePositionalArgs() {
        List<String> positionalParams = List.of("42", "f", "something", "5");
        final SimpleObject baseObj = new SimpleObject(1, true, "baseSo", null);


        Map<String, Object> parsedArgs = new HashMap<>();
        when(literalSupporter.evaluateLiteral("42")).thenReturn(Response.notHandled());
        assertFalse(testee.handlePositionalArgs(positionalParams, parsedArgs));

        when(literalSupporter.evaluateLiteral("42")).thenReturn(Response.is(42));
        when(literalSupporter.evaluateLiteral("f")).thenReturn(Response.is(false));
        when(literalSupporter.evaluateLiteral("something")).thenReturn(Response.is("something"));
        when(literalSupporter.evaluateLiteral("5"))
                .thenReturn(Response.is(5));

        parsedArgs.clear();
        parsedArgs.put(CHAINED_ARG, baseObj);
        assertTrue(testee.handlePositionalArgs(positionalParams, parsedArgs));
        assertEquals(5, parsedArgs.size());
        assertTrue(parsedArgs.containsKey("intVal"));
        assertEquals(42, parsedArgs.get("intVal"));
        assertTrue(parsedArgs.containsKey("boolVal"));
        assertEquals(false, parsedArgs.get("boolVal"));
        assertTrue(parsedArgs.containsKey("strVal"));
        assertEquals("something", parsedArgs.get("strVal"));
        assertTrue(parsedArgs.containsKey("so"));
        assertEquals(baseObj, parsedArgs.get("so"));
        assertTrue(parsedArgs.containsKey("intArrayVal"));
        assertEquals(5, parsedArgs.get("intArrayVal"));

        parsedArgs.clear();
        when(literalSupporter.evaluateLiteral("baseObj")).thenReturn(Response.is(baseObj));
        positionalParams = List.of("42", "f", "something", "baseObj", "5");
        assertTrue(testee.handlePositionalArgs(positionalParams, parsedArgs));
        assertEquals(5, parsedArgs.size());
        assertTrue(parsedArgs.containsKey("so"));
        assertEquals(baseObj, parsedArgs.get("so"));
        assertTrue(parsedArgs.containsKey("intArrayVal"));
        assertEquals(5, parsedArgs.get("intArrayVal"));
        
        when(literalSupporter.evaluateLiteral("so")).thenReturn(Response.is(baseObj));
        positionalParams = List.of("42", "f", "something", "baseObj", "5", "oops");
        assertFalse(testee.handlePositionalArgs(positionalParams, parsedArgs));
    }
}
