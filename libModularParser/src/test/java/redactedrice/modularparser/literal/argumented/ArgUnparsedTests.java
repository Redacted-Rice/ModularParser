package redactedrice.modularparser.literal.argumented;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.Response;

class ArgUnparsedTests {

    @Test
    void preparseEvaluate() {
        ArgUnparsed testee = new ArgUnparsed();
        assertEquals("anything", testee.preparseEvaluate("anything").getValue());
        assertEquals("5", testee.preparseEvaluate("5").getValue());
    }

    @Test
    void tryParseArgument() {
        ArgUnparsed testee = new ArgUnparsed();

        assertTrue(testee.tryParseArgument(Response.is(5), "5").wasError());
        assertTrue(testee.tryParseArgument(Response.notHandled(), "anything").wasError());
    }
}
