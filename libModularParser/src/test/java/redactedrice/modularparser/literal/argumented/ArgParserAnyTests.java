package redactedrice.modularparser.literal.argumented;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.Response;

class ArgParserAnyTests {

    @Test
    void constuctor() {
        ArgParserAny testee = new ArgParserAny(true);
        assertEquals(true, testee.allowNull);

        testee = new ArgParserAny(false);
        assertEquals(false, testee.allowNull);

        testee = new ArgParserAny();
        assertEquals(false, testee.allowNull);
    }

    @Test
    void tryParseArgument() {
        ArgParserAny testee = new ArgParserAny(false);

        assertEquals(5, testee.tryParseArgument(Response.is(5), "5").getValue());
        assertEquals(-3, testee.tryParseArgument(Response.is(-3), "-3").getValue());
        assertEquals("test", testee.tryParseArgument(Response.is("test"), "test").getValue());
    }

    @Test
    void expectedTypes() {
        ArgParserAny testee = new ArgParserAny();
        assertIterableEquals(List.of(Object.class), testee.expectedTypes());
    }
}
