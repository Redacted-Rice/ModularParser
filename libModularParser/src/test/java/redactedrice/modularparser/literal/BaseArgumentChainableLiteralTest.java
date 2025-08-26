package redactedrice.modularparser.literal;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.ModularParser;
import redactedrice.modularparser.testsupport.SimpleObject;
import redactedrice.modularparser.testsupport.SimpleObjectLiteralParser;

public class BaseArgumentChainableLiteralTest {

    private ModularParser parser;
    private LiteralSupporter literalSupporter;
    private BaseArgumentChainableLiteral testee;

    final String CHAINED_ARG = "so";

    @BeforeEach
    void setup() {
        parser = mock(ModularParser.class);
        literalSupporter = mock(LiteralSupporter.class);
        when(parser.getSupporterOfType(LiteralSupporter.class)).thenReturn(literalSupporter);
        testee = spy(new SimpleObjectLiteralParser());
        testee.setParser(parser);
        testee.setModuleRefs();
    }

    @Test
    void constructorSetModuleRefsTest() {
        assertEquals("SimpleObjectParser", testee.getName());
        assertEquals("simpleobject", testee.keyword);
        assertEquals(CHAINED_ARG, testee.chainedArg);
        assertEquals(1, testee.requiredArgs.length);
        assertEquals(3, testee.optionalArgs.length);
        assertEquals(3, testee.optionalDefaults.length);
        assertEquals(literalSupporter, testee.literalSupporter);
    }

    @Test
    void handleObjectLiteralTest() {
        doReturn(true).when(testee).parseArgs(any(), any(), any());
        doReturn(true).when(testee).handlePositionalArgs(any(), any());
        doNothing().when(testee).handleNamedArgs(any(), any());

        Map<String, Object> parsedArgs = new HashMap<>();
        assertFalse(testee.handleObjectLiteral(null, parsedArgs));
        assertFalse(testee.handleObjectLiteral("not matching", parsedArgs));
        assertFalse(testee.handleObjectLiteral("WrongKeyword(\"doesn't matter\")", parsedArgs));

        doReturn(false).when(testee).parseArgs(any(), any(), any());
        assertFalse(testee.handleObjectLiteral("SimpleObject(\"doesn't matter\")", parsedArgs));
        assertFalse(testee.handleObjectLiteral("simpleobject(\"doesn't matter\")", parsedArgs));

        doReturn(true).when(testee).parseArgs(any(), any(), any());
        doReturn(false).when(testee).handlePositionalArgs(any(), any());
        assertFalse(testee.handleObjectLiteral("SimpleObject(\"doesn't matter\")", parsedArgs));

        // Not required args
        doReturn(true).when(testee).handlePositionalArgs(any(), any());
        doNothing().when(testee).handleNamedArgs(any(), any());
        assertFalse(testee.handleObjectLiteral("SimpleObject(\"doesn't matter\")", parsedArgs));

        parsedArgs = new HashMap<>(Map.of("intVal", 42, "strVal", "something"));
        assertTrue(testee.handleObjectLiteral("SimpleObject(\"doesn't matter\")", parsedArgs));
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
        final Optional<Object> EXPECTED = Optional.of("Object");
        doReturn(EXPECTED).when(testee).tryEvaluateObject(any());

        doReturn(false).when(testee).handleObjectLiteral(any(), any());
        assertEquals(Optional.empty(), testee.tryParseLiteral("anything"));
        verify(testee, never()).tryEvaluateObject(any());

        doReturn(true).when(testee).handleObjectLiteral(any(), any());
        assertEquals(EXPECTED, testee.tryParseLiteral("anything"));
        verify(testee).tryEvaluateObject(any());
    }

    @Test
    void tryEvaluateChainedLiteral() {
        final Object BASE_OBJ = "BaseObj";
        final Optional<Object> EXPECTED = Optional.of("Object");
        doReturn(EXPECTED).when(testee).tryEvaluateObject(any());

        doReturn(false).when(testee).handleObjectLiteral(any(), any());
        assertEquals(Optional.empty(), testee.tryEvaluateChainedLiteral(BASE_OBJ, "anything"));
        verify(testee, never()).tryEvaluateObject(any());

        doReturn(true).when(testee).handleObjectLiteral(any(), any());
        assertEquals(EXPECTED, testee.tryEvaluateChainedLiteral(BASE_OBJ, "anything"));
        verify(testee).tryEvaluateObject(
                argThat(map -> map.get(CHAINED_ARG).equals(BASE_OBJ) && map.size() == 1));
    }

    @Test
    void parseArgsTest() {
        String[] args = {"42", "f", "strVal \"something\""};

        List<String> positional = new ArrayList<>();
        Map<String, String> named = new HashMap<>();
        assertTrue(testee.parseArgs(args, positional, named));
        assertEquals(2, positional.size());
        assertTrue(positional.contains("42"));
        assertTrue(positional.contains("f"));
        assertEquals(1, named.size());
        assertEquals("\"something\"", named.get("strVal"));

        assertFalse(testee.parseArgs(new String[] {"intVal 42", "", "f"}, positional, named));
        assertFalse(testee.parseArgs(new String[] {"intVal 42", "boolVal f", "\"some string\""},
                positional, named));
        assertFalse(testee.parseArgs(new String[] {"intVal 42", "boolVal f", "\"some string"},
                positional, named));
    }

    @Test
    void handlePositionalArgsTest() {
        List<String> positionalParams = List.of("42", "f", "something");
        when(literalSupporter.evaluateLiteral("42")).thenReturn(42);
        when(literalSupporter.evaluateLiteral("f")).thenReturn(false);
        when(literalSupporter.evaluateLiteral("something")).thenReturn("something");
        when(literalSupporter.evaluateLiteral("so"))
                .thenReturn(new SimpleObject(5, false, "test", null));

        Map<String, Object> parsedArgs = new HashMap<>();
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
        when(literalSupporter.evaluateLiteral("42")).thenReturn(42);
        when(literalSupporter.evaluateLiteral("something")).thenReturn("something");

        Map<String, Object> parsedArgs = new HashMap<>();
        testee.handleNamedArgs(namedParams, parsedArgs);
        assertEquals(2, parsedArgs.size());
        assertTrue(parsedArgs.containsKey("intVal"));
        assertEquals(42, parsedArgs.get("intVal"));
        assertTrue(parsedArgs.containsKey("strVal"));
        assertEquals("something", parsedArgs.get("strVal"));
    }
}
