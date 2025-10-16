package redactedrice.modularparser.literal.argumented;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.Response;

class ArgParserSingleTypeTests {

    @Test
    void constuctor() {
        ArgParserSingleType testee = new ArgParserTyped<>(Integer.class, true);
        assertEquals(true, testee.allowNull);

        testee = new ArgParserTyped<>(Integer.class, false);
        assertEquals(false, testee.allowNull);
    }

    @Test
    void tryParseArgument() {
        ArgParserSingleType testee = new ArgParserTyped<>(Integer.class, true);

        assertTrue(testee.tryParseArgument(Response.is(5), "doesn't matter").wasValueReturned());
        assertFalse(testee.tryParseArgument(Response.notHandled(), "doesn't matter")
                .wasValueReturned());
        assertFalse(testee.tryParseArgument(Response.is("some string"), "doesn't matter")
                .wasValueReturned());
    }

    @Test
    void expectedTypes() {
        ArgParserSingleType testee = new ArgParserTyped<>(Integer.class);
        assertIterableEquals(List.of(Integer.class), testee.expectedTypes());
    }
}
