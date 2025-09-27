package redactedrice.modularparser.literal.argumented;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.Response;

class ArgumentParserBaseTests {

    @Test
    void constuctor() {
    	ArgumentParserBase testee = new TypeEnforcer<>(true, Integer.class);
        assertEquals(true, testee.allowNull);
        
        testee = new TypeEnforcer<>(false, Integer.class);
        assertEquals(false, testee.allowNull);
    }

    @Test
    void tryParseArgument_nullable() {
    	ArgumentParserBase testee = new TypeEnforcer<>(true, Integer.class);
    	Response<Object> unhandled = Response.notHandled();

        assertTrue(testee.tryParseArgument(Response.is(null), "something").wasHandled());
        assertTrue(testee.tryParseArgument(unhandled, "\t").wasHandled());
        assertTrue(testee.tryParseArgument(unhandled, "null").wasHandled());
    }
    
    @Test
    void tryParseArgument_nonNullable() {
    	ArgumentParserBase testee = new TypeEnforcer<>(false, Integer.class);
    	Response<Object> unhandled = Response.notHandled();
    	
        assertTrue(testee.tryParseArgument(Response.is(null), "something").wasError());
        assertTrue(testee.tryParseArgument(unhandled, "\t").wasError());
        assertTrue(testee.tryParseArgument(unhandled, "null").wasError());
    }
}
