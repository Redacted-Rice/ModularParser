package redactedrice.modularparser.literal.argumented;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.Response;

class ArgParserStreamTests {

    @Test
    void constuctor() {
        ArgParserStream testee = new ArgParserStream(true);
        assertEquals(true, testee.allowNull);

        testee = new ArgParserStream(false);
        assertEquals(false, testee.allowNull);
    }

    @Test
    void tryParseArgument() {
        ArgParserStream testee = new ArgParserStream(false);

        // null not allowed
        assertFalse(
                testee.tryParseArgument(Response.is(null), "doesn't matter").wasValueReturned());

        assertFalse(testee.tryParseArgument(Response.is(5), "doesn't matter").wasValueReturned());
        // List with no values is bad
        assertFalse(testee.tryParseArgument(Response.is(List.of()), "doesn't matter")
                .wasValueReturned());

        assertTrue(testee.tryParseArgument(Response.is(Stream.of(1, 2, 3)), "doesn't matter")
                .wasValueReturned());
    }

    @Test
    void tryParseNonNullArgument() {
        ArgParserStream testee = new ArgParserStream(false);
        assertFalse(testee.tryParseNonNullArgument(Response.notHandled(), "doesn't matter")
                .wasValueReturned());
        assertFalse(testee.tryParseNonNullArgument(Response.is(5), "doesn't matter")
                .wasValueReturned());
        // List with no values is bad
        assertFalse(testee.tryParseNonNullArgument(Response.is(List.of()), "doesn't matter")
                .wasValueReturned());

        assertTrue(testee.tryParseNonNullArgument(Response.is(Stream.of(1, 2, 3)), "doesn't matter")
                .wasValueReturned());
    }

    @Test
    void expectedType() {
        ArgParserStream testee = new ArgParserStream(false);
        assertEquals(Stream.class, testee.expectedType());
    }
}
