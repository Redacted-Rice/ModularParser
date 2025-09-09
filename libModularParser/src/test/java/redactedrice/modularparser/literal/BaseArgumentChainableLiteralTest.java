package redactedrice.modularparser.literal;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    void defaultGrouperTest() {
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
    void constructorSetModuleRefsTest() {
        assertEquals(SimpleObjectLiteralParser.class.getSimpleName(), testee.getName());
        assertEquals("simpleobject", testee.getKeyword());
        assertEquals(grouper, testee.getGrouper());
        assertEquals(CHAINED_ARG, testee.getChainedArg());
        assertEquals(1, testee.getRequiredArgs().length);
        assertEquals(3, testee.getOptionalArgs().length);
        assertEquals(3, testee.getOptionalDefaults().length);
        assertEquals(literalSupporter, testee.getLiteralSupporter());
    }

    // Failing
    @Test
    void handleObjectLiteralTest() {
        Map<String, Object> parsedArgs = new HashMap<>();
        assertFalse(testee.handleObjectLiteral(null, parsedArgs));
        assertFalse(testee.handleObjectLiteral("oneWordNotMatching", parsedArgs));
        assertFalse(testee.handleObjectLiteral("not matching", parsedArgs));
        assertFalse(testee.handleObjectLiteral("WrongKeyword (\"doesn't matter\")", parsedArgs));
        assertFalse(testee.handleObjectLiteral("simpleobject", parsedArgs));

        when(grouper.tryGetNextGroup(any(), anyBoolean())).thenReturn(Response.notHandled());
        assertFalse(testee.handleObjectLiteral("simpleobject (\"doesn't matter\")", parsedArgs));
        when(grouper.tryGetNextGroup(any(), anyBoolean()))
                .thenReturn(Response.is(new String[] {"", "\"doesn't matter\"", "not empty"}));
        assertFalse(testee.handleObjectLiteral("simpleobject (\"doesn't matter\")", parsedArgs));
        when(grouper.tryGetNextGroup(any(), anyBoolean()))
                .thenReturn(Response.is(new String[] {"also not empty", "\"doesn't matter\"", ""}));
        assertFalse(testee.handleObjectLiteral("simpleobject (\"doesn't matter\")", parsedArgs));

        when(grouper.tryGetNextGroup(any(), anyBoolean()))
                .thenReturn(Response.is(new String[] {"", "\"doesn't matter\"", ""}));
        // when(testee.parseArgs(any(), any(), any())).thenReturn(false);
        assertFalse(testee.handleObjectLiteral("SimpleObject (\"doesn't matter\")", parsedArgs));
        assertFalse(testee.handleObjectLiteral("simpleobject (\"doesn't matter\")", parsedArgs));

        // when(testee.parseArgs(any(), any(), any())).thenReturn(true);
        // when(testee.handlePositionalArgs(any(), any())).thenReturn(false);
        assertFalse(testee.handleObjectLiteral("SimpleObject (\"doesn't matter\")", parsedArgs));

        // Not required args
        // when(testee.handlePositionalArgs(any(), any())).thenReturn(true);
        // when(testee.handleNamedArgs(any(), any())).thenReturn(false);
        assertFalse(testee.handleObjectLiteral("SimpleObject (\"doesn't matter\")", parsedArgs));

        parsedArgs = new HashMap<>(Map.of("intVal", 42, "strVal", "something"));
        assertTrue(testee.handleObjectLiteral("SimpleObject (\"doesn't matter\")", parsedArgs));
        assertEquals(4, parsedArgs.size());
        assertEquals(42, parsedArgs.get("intVal"));
        assertEquals("something", parsedArgs.get("strVal"));
        assertEquals(false, parsedArgs.get("boolVal"));
        assertEquals(null, parsedArgs.get("so"));

        assertTrue(testee.handleObjectLiteral(
                "SIMpleObjeCT  (\"spaces and mixed cases should work too\")", parsedArgs));
    }

    @Test
    void tryParseLiteralTest() {
        assertEquals(Response.notHandled(), testee.tryParseLiteral("anything"));

        when(grouper.tryGetNextGroup(any(), anyBoolean()))
                .thenReturn(Response.is(new String[] {"", "1", ""}));
        when(grouper.hasOpenGroup(any())).thenReturn(false);
        when(literalSupporter.evaluateLiteral(any())).thenReturn(Response.is(1));
        Response<Object> res = testee.tryParseLiteral("simpleobject (1)");
        assertTrue(res.wasValueReturned());
        assertEquals(1, ((SimpleObject) res.getValue()).intField);
    }

    // Failing
    @Test
    void tryEvaluateChainedLiteralTest() {
        final Object baseObj = "BaseObj";
        final Response<Object> expected = Response.is("Object");
        // doReturn(expected).when(testee).tryEvaluateObject(any());

        // doReturn(false).when(testee).handleObjectLiteral(any(), any());
        assertEquals(Response.notHandled(), testee.tryEvaluateChainedLiteral(baseObj, "anything"));
        verify(testee, never()).tryEvaluateObject(any());

        // doReturn(true).when(testee).handleObjectLiteral(any(), any());
        assertEquals(expected, testee.tryEvaluateChainedLiteral(baseObj, "anything"));
        verify(testee).tryEvaluateObject(
                argThat(map -> map.get(CHAINED_ARG).equals(baseObj) && map.size() == 1));
    }

    @Test
    void parseArgsTest() {
        when(grouper.hasOpenGroup(any())).thenReturn(false);
        String args = "42, f, strVal \"something\"";

        List<String> positional = new ArrayList<>();
        Map<String, String> named = new HashMap<>();
        assertTrue(testee.parseArgs(args, positional, named));
        assertEquals(2, positional.size());
        assertTrue(positional.contains("42"));
        assertTrue(positional.contains("f"));
        assertEquals(1, named.size());
        assertEquals("\"something\"", named.get("strVal"));

        assertFalse(testee.parseArgs("intVal 42,, ,f", positional, named));
        assertFalse(testee.parseArgs("intVal 42, boolVal f, \"some string\"", positional, named));
        assertFalse(testee.parseArgs("intVal 42, boolVal f, \"some string", positional, named));
    }

    @Test
    void tryAddParamTest() {
        // TODO: Add tests here
    }

    @Test
    void handlePositionalArgsTest() {
        List<String> positionalParams = List.of("42", "f", "something");

        Map<String, Object> parsedArgs = new HashMap<>();
        when(literalSupporter.evaluateLiteral("42")).thenReturn(Response.notHandled());
        assertFalse(testee.handlePositionalArgs(positionalParams, parsedArgs));

        when(literalSupporter.evaluateLiteral("42")).thenReturn(Response.is(42));
        when(literalSupporter.evaluateLiteral("f")).thenReturn(Response.is(false));
        when(literalSupporter.evaluateLiteral("something")).thenReturn(Response.is("something"));
        when(literalSupporter.evaluateLiteral("so"))
                .thenReturn(Response.is(new SimpleObject(5, false, "test", null)));
        when(literalSupporter.evaluateLiteral("oops")).thenReturn(Response.is("foundValThough"));

        parsedArgs = new HashMap<>();
        assertTrue(testee.handlePositionalArgs(positionalParams, parsedArgs));
        assertEquals(3, parsedArgs.size());
        assertTrue(parsedArgs.containsKey("intVal"));
        assertEquals(42, parsedArgs.get("intVal"));
        assertTrue(parsedArgs.containsKey("boolVal"));
        assertEquals(false, parsedArgs.get("boolVal"));
        assertTrue(parsedArgs.containsKey("strVal"));
        assertEquals("something", parsedArgs.get("strVal"));

        positionalParams = List.of("42", "f", "something", "so", "oops");
        assertFalse(testee.handlePositionalArgs(positionalParams, parsedArgs));
    }

    @Test
    void handleNamedArgsTest() {
        Map<String, String> namedParams = Map.of("intVal", "42", "strVal", "something");

        Map<String, Object> parsedArgs = new HashMap<>();
        // Depending on map used, either may be returned
        when(literalSupporter.evaluateLiteral("42")).thenReturn(Response.notHandled());
        when(literalSupporter.evaluateLiteral("something")).thenReturn(Response.notHandled());
        assertFalse(testee.handleNamedArgs(namedParams, parsedArgs));

        when(literalSupporter.evaluateLiteral("42")).thenReturn(Response.is(42));
        when(literalSupporter.evaluateLiteral("something")).thenReturn(Response.is("something"));

        parsedArgs = new HashMap<>();
        assertTrue(testee.handleNamedArgs(namedParams, parsedArgs));
        assertEquals(2, parsedArgs.size());
        assertTrue(parsedArgs.containsKey("intVal"));
        assertEquals(42, parsedArgs.get("intVal"));
        assertTrue(parsedArgs.containsKey("strVal"));
        assertEquals("something", parsedArgs.get("strVal"));
    }
}
