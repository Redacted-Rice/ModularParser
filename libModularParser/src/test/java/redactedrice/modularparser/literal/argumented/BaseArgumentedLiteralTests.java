package redactedrice.modularparser.literal.argumented;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
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
import redactedrice.modularparser.literal.LiteralSupporter;
import redactedrice.modularparser.testsupport.SimpleObject;
import redactedrice.modularparser.testsupport.SimpleObjectTypedLiteralParser;
import redactedrice.modularparser.testsupport.SimpleObjectUnchainedLiteralParser;

class BaseArgumentedLiteralTests {

    private ModularParser parser;
    private LiteralSupporter literalSupporter;
    private Grouper grouper;
    private BaseArgumentedLiteral testee;

    static final String CHAINED_ARG = "so";

    @BeforeEach
    void setup() {
        parser = mock(ModularParser.class);
        literalSupporter = mock(LiteralSupporter.class);
        grouper = mock(Grouper.class);
        when(parser.getSupporterOfType(LiteralSupporter.class)).thenReturn(literalSupporter);

        testee = new SimpleObjectUnchainedLiteralParser(grouper);
        testee.setParser(parser);
        testee.setModuleRefs();
    }

    @Test
    void defaultGrouper() {
        // Ensure default is null to keep order of tests from mattering
        BaseArgumentedLiteral.setDefaultGrouper(null);
        assertNull(BaseArgumentedLiteral.getDefaultGrouper());

        BaseArgumentedLiteral.setDefaultGrouper(grouper);
        assertEquals(grouper, BaseArgumentedLiteral.getDefaultGrouper());
        BaseArgumentedLiteral defaultGrouper = new SimpleObjectUnchainedLiteralParser();
        assertEquals(grouper, defaultGrouper.getGrouper());

        // Set it back to null for other tests and test that constructor ensures not null
        BaseArgumentedLiteral.setDefaultGrouper(null);
        assertThrows(IllegalArgumentException.class, SimpleObjectUnchainedLiteralParser::new);
    }

    @Test
    void constructor_setModuleRefs() {
        assertEquals(SimpleObjectUnchainedLiteralParser.class.getSimpleName(), testee.getName());
        assertEquals("simpleobject", testee.getKeyword());
        assertEquals(grouper, testee.getGrouper());
        assertEquals(1, testee.getArgsDef().getNumRequiredArgs());
        assertEquals(3, testee.getArgsDef().getNumOptionalArgs());
        assertEquals(literalSupporter, testee.getLiteralSupporter());
    }

    @Test
    void handleObjectLiteral() {
        Map<String, Object> parsedArgs = new HashMap<>();
        // null/empty literal
        assertFalse(testee.handleObjectLiteral(null, parsedArgs));
        assertFalse(testee.handleObjectLiteral("   ", parsedArgs));

        // Not matching literal
        assertFalse(testee.handleObjectLiteral("oneWordNotMatching", parsedArgs));
        assertFalse(testee.handleObjectLiteral("not matching", parsedArgs));
        assertFalse(testee.handleObjectLiteral("WrongKeyword (\"doesn't matter\")", parsedArgs));
        assertFalse(testee.handleObjectLiteral("simpleobject", parsedArgs));

        // Failed to get grouper
        when(grouper.tryGetNextGroup(any(), anyBoolean())).thenReturn(Response.notHandled());
        assertFalse(testee.handleObjectLiteral("simpleobject (\"doesn't matter\")", parsedArgs));
        when(grouper.tryGetNextGroup(any(), anyBoolean()))
                .thenReturn(Response.is(new String[] {"", "\"doesn't matter\"", "not empty"}));
        assertFalse(testee.handleObjectLiteral("simpleobject (\"doesn't matter\")", parsedArgs));
        when(grouper.tryGetNextGroup(any(), anyBoolean()))
                .thenReturn(Response.is(new String[] {"also not empty", "\"doesn't matter\"", ""}));
        assertFalse(testee.handleObjectLiteral("simpleobject (\"doesn't matter\")", parsedArgs));

        // Bad args - out of order
        when(grouper.tryGetNextGroup(any(), anyBoolean()))
                .thenReturn(Response.is(new String[] {"", "intVal 5, false", ""}));
        assertFalse(testee.handleObjectLiteral("simpleobject (intVal 5, false)", parsedArgs));

        // Fail to parse a positional arg
        when(grouper.tryGetNextGroup(any(), anyBoolean()))
                .thenReturn(Response.is(new String[] {"", "5", ""}));
        when(literalSupporter.evaluateLiteral(any())).thenReturn(Response.notHandled());
        assertFalse(testee.handleObjectLiteral("SimpleObject (5)", parsedArgs));

        // Fail to parse a named arg
        when(grouper.tryGetNextGroup(any(), anyBoolean()))
                .thenReturn(Response.is(new String[] {"", "intVal 5", ""}));
        assertFalse(testee.handleObjectLiteral("SimpleObject (intVal 5)", parsedArgs));

        // Missing required args
        when(grouper.tryGetNextGroup(any(), anyBoolean()))
                .thenReturn(Response.is(new String[] {"", "", ""}));
        assertFalse(testee.handleObjectLiteral("SimpleObject ()", parsedArgs));

        // Good case - has required args
        parsedArgs.clear(); // ensure its clear before testing
        when(grouper.tryGetNextGroup(any(), anyBoolean()))
                .thenReturn(Response.is(new String[] {"", "5, strVal \"something\"", ""}));
        when(literalSupporter.evaluateLiteral("5")).thenReturn(Response.is(5));
        when(literalSupporter.evaluateLiteral("\"something\""))
                .thenReturn(Response.is("something"));
        assertTrue(testee.handleObjectLiteral("SimpleObject (5, boolVal true)", parsedArgs));
        assertEquals(4, parsedArgs.size());
        assertEquals(5, parsedArgs.get("intVal"));
        assertEquals("something", parsedArgs.get("strVal"));
        assertEquals(false, parsedArgs.get("boolVal")); // default arg
        assertNull(parsedArgs.get("so")); // default arg
    }

    @Test
    void tryParseLiteral() {
        assertEquals(Response.notHandled(), testee.tryParseLiteral("anything"));

        when(grouper.tryGetNextGroup(any(), anyBoolean()))
                .thenReturn(Response.is(new String[] {"", "1", ""}));
        when(grouper.hasOpenGroup(any())).thenReturn(false);
        when(literalSupporter.evaluateLiteral(any())).thenReturn(Response.is(1));
        Response<Object> res = testee.tryParseLiteral("simpleobject (1)");
        assertTrue(res.wasValueReturned());
        assertEquals(1, ((SimpleObject) res.getValue()).intField);

        // Pass a bad class
        when(literalSupporter.evaluateLiteral(any())).thenReturn(Response.is("oops"));
        res = testee.tryParseLiteral("simpleobject (\"oops\")");
        assertTrue(res.wasError());
    }

    @Test
    void parseArgs() {
        when(grouper.hasOpenGroup(any())).thenReturn(false);
        when(grouper.hasOpenGroup("SimpleObject (3")).thenReturn(true);
        when(grouper.hasOpenGroup("so SimpleObject (2")).thenReturn(true);
        when(grouper.startsWithAGroup(any())).thenReturn(false);
        when(grouper.startsWithAGroup("()")).thenReturn(true);
        when(grouper.startsWithAGroup("(3, true)")).thenReturn(true);

        String args = "42, SimpleObject (), SimpleObject (3, true), strVal \"something\", so SimpleObject (2, false)";
        List<String> positional = new ArrayList<>();
        Map<String, String> named = new HashMap<>();
        assertTrue(testee.parseArgs(args, positional, named));
        assertEquals(3, positional.size());
        assertEquals("42", positional.get(0));
        assertEquals("SimpleObject ()", positional.get(1));
        assertEquals("SimpleObject (3, true)", positional.get(2));
        assertEquals("\"something\"", named.get("strVal"));
        assertEquals("SimpleObject (2, false)", named.get("so"));
        positional.clear();
        named.clear();

        args = "42, 3";
        assertTrue(testee.parseArgs(args, positional, named));
        assertEquals(2, positional.size());
        assertEquals("42", positional.get(0));
        assertEquals("3", positional.get(1));
        assertTrue(named.isEmpty());
        positional.clear();
        named.clear();

        assertFalse(testee.parseArgs("intVal 42,, ,f", positional, named));
        assertFalse(testee.parseArgs("intVal 42, boolVal f, \"some string\"", positional, named));
        assertFalse(testee.parseArgs("intVal 42, boolVal f, \"some string", positional, named));
        assertFalse(testee.parseArgs("intVal 42, SimpleObject (3, true)", positional, named));

        // Check the ill formed case
        when(grouper.hasOpenGroup(any())).thenReturn(true);
        assertFalse(testee.parseArgs("\"have and empty arg\",", positional, named));
    }

    @Test
    void parseArgs_commandChain() {
        String args = "do Select (foo bar, value so) -> Set (field intVal, to 5)";

        when(grouper.hasOpenGroup(any())).thenReturn(false);
        when(grouper.hasOpenGroup("do Select (foo bar")).thenReturn(true);
        when(grouper.hasOpenGroup("do Select (foo bar, value so) -> Set (field intVal"))
                .thenReturn(true);
        when(grouper.startsWithAGroup(any())).thenReturn(false);

        List<String> positional = new ArrayList<>();
        Map<String, String> named = new HashMap<>();
        assertTrue(testee.parseArgs(args, positional, named));
        assertEquals(0, positional.size());
        assertEquals(1, named.size());
        assertEquals("Select (foo bar, value so) -> Set (field intVal, to 5)", named.get("do"));
    }

    @Test
    void tryAddParam_literal() {
        List<String> positional = new ArrayList<>();
        Map<String, String> named = new HashMap<>();
        assertTrue(testee.tryAddParam("2", positional, named, true).wasError());
        assertTrue(testee.tryAddParam("'2'", positional, named, true).wasError());
        assertTrue(testee.tryAddParam("\"string\"", positional, named, true).wasError());

        when(grouper.isEmptyGroup(any())).thenReturn(false);
        Response<Boolean> res = testee.tryAddParam("intVal 2", positional, named, false);
        assertTrue(res.wasValueReturned());
        assertTrue(res.getValue());
        assertTrue(positional.isEmpty());
        assertEquals("2", named.get("intVal"));
        named.clear();

        res = testee.tryAddParam("intVal 2", positional, named, true);
        assertTrue(res.wasValueReturned());
        assertTrue(res.getValue());
        assertTrue(positional.isEmpty());
        assertEquals("2", named.get("intVal"));
        named.clear();

        res = testee.tryAddParam("2", positional, named, false);
        assertTrue(res.wasValueReturned());
        assertFalse(res.getValue());
        assertEquals("2", positional.get(0));
        assertTrue(named.isEmpty());
        positional.clear();

        res = testee.tryAddParam("\"string (with lots of spaces and confusing syntax)\"",
                positional, named, false);
        assertTrue(res.wasValueReturned());
        assertFalse(res.getValue());
        assertEquals("\"string (with lots of spaces and confusing syntax)\"", positional.get(0));
        assertTrue(named.isEmpty());
        positional.clear();

        res = testee.tryAddParam("named \"(namedstring (with spaces and) odd syntax)\"", positional,
                named, false);
        assertTrue(res.wasValueReturned());
        assertTrue(res.getValue());
        assertTrue(positional.isEmpty());
        assertEquals("\"(namedstring (with spaces and) odd syntax)\"", named.get("named"));
        named.clear();
    }

    @Test
    void tryAddParam_object() {
        List<String> positional = new ArrayList<>();
        Map<String, String> named = new HashMap<>();

        when(grouper.startsWithAGroup(any())).thenReturn(false);
        when(grouper.startsWithAGroup("(2)")).thenReturn(true);
        Response<Boolean> res = testee.tryAddParam("so SimpleObject (2)", positional, named, false);
        assertTrue(res.wasValueReturned());
        assertTrue(res.getValue());
        assertTrue(positional.isEmpty());
        assertEquals("SimpleObject (2)", named.get("so"));
        named.clear();

        res = testee.tryAddParam("so SimpleObject (2)", positional, named, true);
        assertTrue(res.wasValueReturned());
        assertTrue(res.getValue());
        assertTrue(positional.isEmpty());
        assertEquals("SimpleObject (2)", named.get("so"));
        named.clear();

        res = testee.tryAddParam("SimpleObject (2)", positional, named, false);
        assertTrue(res.wasValueReturned());
        assertFalse(res.getValue());
        assertEquals("SimpleObject (2)", positional.get(0));
        assertTrue(named.isEmpty());
        positional.clear();
    }

    @Test
    void tryAddParam_badParams() {
        List<String> positional = new ArrayList<>();
        Map<String, String> named = new HashMap<>();

        when(grouper.startsWithAGroup(any())).thenReturn(false);
        when(grouper.startsWithAGroup("(5, false)")).thenReturn(true);
        when(grouper.startsWithAGroup("()")).thenReturn(true);

        // Already found a named param
        assertTrue(
                testee.tryAddParam("SimpleObject (5, false)", positional, named, true).wasError());
        assertTrue(testee.tryAddParam("SimpleObject ()", positional, named, true).wasError());

        // Bad string
        assertTrue(testee.tryAddParam("\"no close string", positional, named, true).wasError());
        assertTrue(testee.tryAddParam("'E", positional, named, true).wasError());
        assertTrue(testee.tryAddParam("\"mixed str'", positional, named, true).wasError());
        assertTrue(testee.tryAddParam("'E\"", positional, named, true).wasError());
    }

    @Test
    void tryAddParam_commandChain() {
        List<String> positional = new ArrayList<>();
        Map<String, String> named = new HashMap<>();

        // A longer, multiple operation arg
        when(grouper.isEmptyGroup(any())).thenReturn(false);
        Response<Boolean> res = testee.tryAddParam(
                "do Select (value so) -> Set (field intVal, to 5)", positional, named, false);
        assertTrue(res.wasValueReturned());
        assertTrue(res.getValue());
        assertTrue(positional.isEmpty());
        assertEquals("Select (value so) -> Set (field intVal, to 5)", named.get("do"));
    }

    @Test
    void handlePositionalArgs() {
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
    void handleNamedArgs() {
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

    @Test
    void tryParseArgument() {
        testee = new SimpleObjectTypedLiteralParser(grouper);
        testee.setParser(parser);
        testee.setModuleRefs();

        Map<String, Object> parsedArgs = new HashMap<>();
        assertFalse(testee.tryParseArgument("badName", "anything", parsedArgs));

        Object val = 5;
        String valStr = "5";
        String name = "intVal";
        when(literalSupporter.evaluateLiteral(valStr)).thenReturn(Response.is(val));
        assertTrue(testee.tryParseArgument(name, valStr, parsedArgs));
        assertEquals(val, parsedArgs.get(name));

        when(literalSupporter.evaluateLiteral(valStr)).thenReturn(Response.notHandled());
        assertFalse(testee.tryParseArgument(name, valStr, parsedArgs));

        when(literalSupporter.evaluateLiteral(valStr)).thenReturn(Response.is("wrong type"));
        assertFalse(testee.tryParseArgument(name, valStr, parsedArgs));

        val = true;
        valStr = "true";
        name = "boolVal";
        when(literalSupporter.evaluateLiteral(valStr)).thenReturn(Response.is(val));
        assertTrue(testee.tryParseArgument(name, valStr, parsedArgs));
        assertEquals(val, parsedArgs.get(name));

        valStr = "so1";
        name = "so";
        when(literalSupporter.evaluateLiteral(valStr)).thenReturn(Response.notHandled());
        assertTrue(testee.tryParseArgument(name, valStr, parsedArgs));
        SimpleObject so = (SimpleObject) parsedArgs.get(name);
        assertEquals(valStr, so.strField);
    }

    @Test
    void tryParseArgument_specialCases() {
        testee = mock(BaseArgumentedLiteral.class);
        doCallRealMethod().when(testee).tryParseArgument(any(), any(), any());

        when(literalSupporter.evaluateLiteral(any())).thenReturn(Response.notHandled());
        when(testee.getLiteralSupporter()).thenReturn(literalSupporter);

        ArgumentParser mockArg = mock(ArgumentParser.class);
        when(mockArg.preparseEvaluate(any())).thenReturn(Response.notHandled());
        when(mockArg.tryParseArgument(any(), any())).thenReturn(Response.notHandled());
        when(testee.getArgParser(any())).thenReturn(mockArg);

        Map<String, Object> parsedArgs = new HashMap<>();
        Object val = 5;
        String valStr = "5";
        String name = "intVal";
        when(literalSupporter.evaluateLiteral(valStr)).thenReturn(Response.is(val));
        assertFalse(testee.tryParseArgument(name, valStr, parsedArgs));

        // Test an uparsed case
        ArgumentParser mockUnparsed = mock(ArgumentParser.class);
        when(mockUnparsed.preparseEvaluate(valStr)).thenReturn(Response.is(valStr));
        when(mockUnparsed.tryParseArgument(any(), any())).thenReturn(Response.notHandled());
        when(testee.getArgParser(any())).thenReturn(mockUnparsed);

        assertTrue(testee.tryParseArgument(name, valStr, parsedArgs));
        String unparsed = (String) parsedArgs.get(name);
        assertEquals(valStr, unparsed);
    }
}
