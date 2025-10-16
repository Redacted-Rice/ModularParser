package redactedrice.modularparser.literal.argumented;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.Response;

class ArgParserTypedTests {

    @Test
    void constuctor() {
        ArgParserTyped<Integer> testee = new ArgParserTyped<>(Integer.class);
        assertEquals(Integer.class, testee.clazz);
    }

    @Test
    void tryParseArgument() {
        ArgParserTyped<Integer> testee = new ArgParserTyped<>(Integer.class, true);
        assertTrue(testee.tryParseArgument(Response.notHandled(), "null").wasValueReturned());
        testee = new ArgParserTyped<>(Integer.class, false);
        assertFalse(testee.tryParseArgument(Response.notHandled(), "null").wasValueReturned());

        assertTrue(testee.tryParseArgument(Response.is(5), "doesn't matter").wasValueReturned());
        assertFalse(
                testee.tryParseArgument(Response.is("bad"), "doesn't matter").wasValueReturned());
    }
}
