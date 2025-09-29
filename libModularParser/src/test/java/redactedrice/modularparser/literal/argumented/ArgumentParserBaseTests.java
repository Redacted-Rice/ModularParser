package redactedrice.modularparser.literal.argumented;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.Response;

class ArgumentParserBaseTests {

    @Test
    void constuctor() {
        ArgumentParserBase testee = new TypeEnforcer<>(Integer.class, true);
        assertEquals(true, testee.allowNull);

        testee = new TypeEnforcer<>(Integer.class, false);
        assertEquals(false, testee.allowNull);

        testee = new TypeEnforcer<>(Integer.class);
        assertEquals(false, testee.allowNull);
    }

    @Test
    void tryParseArgument_type() {
        ArgumentParserBase testee = new TypeEnforcer<>(Integer.class);
        assertTrue(testee.tryParseArgument(Response.is(5), "doesn't matter").wasHandled());
        assertTrue(testee.tryParseArgument(Response.is("bad"), "doesn't matter").wasError());
    }

    @Test
    void tryParseArgument_nullable() {
        ArgumentParserBase testee = new TypeEnforcer<>(Integer.class, true);
        Response<Object> unhandled = Response.notHandled();

        assertTrue(testee.tryParseArgument(Response.is(null), "something").wasHandled());
        assertTrue(testee.tryParseArgument(unhandled, "\t").wasHandled());
        assertTrue(testee.tryParseArgument(unhandled, "null").wasHandled());
    }

    @Test
    void tryParseArgument_nonNullable() {
        ArgumentParserBase testee = new TypeEnforcer<>(Integer.class, false);
        Response<Object> unhandled = Response.notHandled();

        assertTrue(testee.tryParseArgument(Response.is(null), "something").wasError());
        assertTrue(testee.tryParseArgument(unhandled, "\t").wasError());
        assertTrue(testee.tryParseArgument(unhandled, "null").wasError());
    }
}
