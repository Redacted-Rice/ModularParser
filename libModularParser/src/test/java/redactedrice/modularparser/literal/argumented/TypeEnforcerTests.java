package redactedrice.modularparser.literal.argumented;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import redactedrice.modularparser.core.Response;

class TypeEnforcerTests {

    @Test
    void constuctor() {
    	TypeEnforcer<Integer> testee = new TypeEnforcer<>(Integer.class);
        assertEquals(Integer.class, testee.clazz);
    }
    
    @Test
    void tryParseArgument() {
    	TypeEnforcer<Integer> testee = new TypeEnforcer<>(Integer.class, true);
        assertTrue(testee.tryParseArgument(Response.notHandled(), "null").wasHandled());
    	testee = new TypeEnforcer<>(Integer.class, false);
        assertTrue(testee.tryParseArgument(Response.notHandled(), "null").wasError());
    	
        assertTrue(testee.tryParseArgument(Response.is(5), "doesn't matter").wasHandled());
        assertTrue(testee.tryParseArgument(Response.is("bad"), "doesn't matter").wasError());
    }
}
