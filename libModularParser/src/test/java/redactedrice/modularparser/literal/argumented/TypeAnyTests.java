package redactedrice.modularparser.literal.argumented;


import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.Response;

class TypeAnyTests {

    @Test
    void tryParseArgument() {
    	// Test null
    	ArgParserAny testee = new ArgParserAny(true);
        assertTrue(testee.tryParseArgument(Response.notHandled(), "null").wasHandled());
        testee = new ArgParserAny(false);
        assertTrue(testee.tryParseArgument(Response.notHandled(), "null").wasError());
    	
        // Test return objects - anything is accepted
        assertTrue(testee.tryParseArgument(Response.is(5), "anything").wasHandled());
        assertTrue(testee.tryParseArgument(Response.is("some string"), "anything").wasHandled());
    }
}
